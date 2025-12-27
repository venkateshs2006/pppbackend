package com.java.ppp.pppbackend.service;


import com.java.ppp.pppbackend.dto.CreateNotificationRequest;
import com.java.ppp.pppbackend.dto.NotificationDTO;
import com.java.ppp.pppbackend.dto.NotificationPreferenceDTO;
import com.java.ppp.pppbackend.entity.*;
import com.java.ppp.pppbackend.exception.ResourceNotFoundException;
import com.java.ppp.pppbackend.repository.NotificationPreferenceRepository;
import com.java.ppp.pppbackend.repository.NotificationRepository;
import com.java.ppp.pppbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private NotificationPreferenceRepository preferenceRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Create and send notification
     */
    @Transactional
    @Async
    public void createNotification(CreateNotificationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check user preferences
        NotificationPreference preference = getOrCreatePreference(user.getId());

        // Create notification
        Notification notification = Notification.builder()
                .user(user)
                .type(request.getType())
                .title(request.getTitle())
                .message(request.getMessage())
                .linkUrl(request.getLinkUrl())
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .priority(request.getPriority() != null ? request.getPriority() : NotificationPriority.NORMAL)
                .build();

        Notification saved = notificationRepository.save(notification);

        // Send in-app notification via WebSocket
        if (preference.getInAppEnabled()) {
            sendRealtimeNotification(user.getId(), saved);
        }

        // Send email notification
        if (preference.getEmailEnabled() && shouldSendEmailForType(preference, request.getType())) {
            sendEmailNotification(user, saved);
        }

        log.info("Notification created for user: {} of type: {}", user.getUsername(), request.getType());
    }

    /**
     * Send notification to multiple users
     */
    @Transactional
    @Async
    public void createBulkNotification(List<Long> userIds, CreateNotificationRequest request) {
        userIds.forEach(userId -> {
            CreateNotificationRequest userRequest = CreateNotificationRequest.builder()
                    .userId(userId)
                    .type(request.getType())
                    .title(request.getTitle())
                    .message(request.getMessage())
                    .linkUrl(request.getLinkUrl())
                    .entityType(request.getEntityType())
                    .entityId(request.getEntityId())
                    .priority(request.getPriority())
                    .build();
            createNotification(userRequest);
        });
    }

    /**
     * Get user notifications
     */
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getUserNotifications(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(this::mapToDTO);
    }

    /**
     * Get unread notifications
     */
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getUnreadNotifications(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository
                .findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false, pageable);
        return notifications.map(this::mapToDTO);
    }

    /**
     * Get notifications by type
     */
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getNotificationsByType(
            Long userId, NotificationType type, Pageable pageable) {
        Page<Notification> notifications = notificationRepository
                .findByUserIdAndTypeOrderByCreatedAtDesc(userId, type, pageable);
        return notifications.map(this::mapToDTO);
    }

    /**
     * Get unread count
     */
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadNotifications(userId);
    }

    /**
     * Mark notification as read
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.markAsRead(notificationId, LocalDateTime.now());

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        // Send WebSocket update
        sendUnreadCountUpdate(notification.getUser().getId());
    }

    /**
     * Mark all notifications as read
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId, LocalDateTime.now());

        // Send WebSocket update
        sendUnreadCountUpdate(userId);
    }

    /**
     * Delete notification
     */
    @Transactional
    public void deleteNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        notificationRepository.delete(notification);
        sendUnreadCountUpdate(notification.getUser().getId());
    }

    /**
     * Get notification preferences
     */
    @Transactional(readOnly = true)
    public NotificationPreferenceDTO getPreferences(Long userId) {
        NotificationPreference preference = getOrCreatePreference(userId);
        return mapPreferenceToDTO(preference);
    }

    /**
     * Update notification preferences
     */
    @Transactional
    public NotificationPreferenceDTO updatePreferences(
            Long userId, NotificationPreferenceDTO preferenceDTO) {
        NotificationPreference preference = getOrCreatePreference(userId);

        preference.setEmailEnabled(preferenceDTO.getEmailEnabled());
        preference.setInAppEnabled(preferenceDTO.getInAppEnabled());
        preference.setPushEnabled(preferenceDTO.getPushEnabled());
        preference.setContentUpdates(preferenceDTO.getContentUpdates());
        preference.setCommentsEnabled(preferenceDTO.getCommentsEnabled());
        preference.setMentionsEnabled(preferenceDTO.getMentionsEnabled());
        preference.setWorkflowNotifications(preferenceDTO.getWorkflowNotifications());
        preference.setSystemAlerts(preferenceDTO.getSystemAlerts());
        preference.setDigestFrequency(preferenceDTO.getDigestFrequency());
        preference.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

        NotificationPreference updated = preferenceRepository.save(preference);
        return mapPreferenceToDTO(updated);
    }

    /**
     * Send realtime notification via WebSocket
     */
    private void sendRealtimeNotification(Long userId, Notification notification) {
        try {
            NotificationDTO dto = mapToDTO(notification);
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications",
                    dto
            );
            log.info("Realtime notification sent to user: {}", userId);
        } catch (Exception e) {
            log.error("Error sending realtime notification to user: {}", userId, e);
        }
    }

    /**
     * Send unread count update via WebSocket
     */
    private void sendUnreadCountUpdate(Long userId) {
        try {
            Long unreadCount = getUnreadCount(userId);
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/unread-count",
                    unreadCount
            );
        } catch (Exception e) {
            log.error("Error sending unread count update to user: {}", userId, e);
        }
    }

    /**
     * Send email notification
     */
    private void sendEmailNotification(User user, Notification notification) {
        try {
            String linkUrl = notification.getLinkUrl() != null ?
                    notification.getLinkUrl() : "http://localhost:8080/notifications";

            emailService.sendContentNotificationEmail(
                    user.getEmail(),
                    notification.getTitle(),
                    notification.getMessage(),
                    linkUrl
            );

            notification.setIsSent(true);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
        } catch (Exception e) {
            log.error("Error sending email notification to user: {}", user.getEmail(), e);
        }
    }

    /**
     * Check if email should be sent for notification type
     */
    private boolean shouldSendEmailForType(NotificationPreference preference, NotificationType type) {
        return switch (type) {
            case CONTENT_PUBLISHED, CONTENT_APPROVED -> preference.getContentUpdates();
            case CONTENT_COMMENTED, MENTION -> preference.getCommentsEnabled() || preference.getMentionsEnabled();
            case WORKFLOW_ASSIGNED, WORKFLOW_APPROVED, WORKFLOW_REJECTED -> preference.getWorkflowNotifications();
            case SYSTEM_ALERT -> preference.getSystemAlerts();
            default -> true;
        };
    }

    /**
     * Get or create notification preference
     */
    private NotificationPreference getOrCreatePreference(Long userId) {
        return preferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                    NotificationPreference preference = NotificationPreference.builder()
                            .user(user)
                            .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                            .build();
                    return preferenceRepository.save(preference);
                });
    }

    /**
     * Scheduled task to clean up expired notifications
     */
    @Scheduled(cron = "0 0 2 * * *") // Run at 2 AM daily
    @Transactional
    public void cleanupExpiredNotifications() {
        try {
            notificationRepository.deleteExpiredNotifications(LocalDateTime.now());
            log.info("Expired notifications cleaned up successfully");
        } catch (Exception e) {
            log.error("Error cleaning up expired notifications", e);
        }
    }

    /**
     * Scheduled task to send pending notifications
     */
    @Scheduled(fixedDelay = 60000) // Run every minute
    @Transactional
    public void sendPendingNotifications() {
        try {
            LocalDateTime before = LocalDateTime.now().minusMinutes(1);
            List<Notification> pending = notificationRepository
                    .findByIsSentFalseAndCreatedAtBefore(before);

            pending.forEach(notification -> {
                User user = notification.getUser();
                NotificationPreference preference = getOrCreatePreference(user.getId());

                if (preference.getEmailEnabled() &&
                        shouldSendEmailForType(preference, notification.getType())) {
                    sendEmailNotification(user, notification);
                }
            });

            if (!pending.isEmpty()) {
                log.info("Sent {} pending notifications", pending.size());
            }
        } catch (Exception e) {
            log.error("Error sending pending notifications", e);
        }
    }

    /**
     * Helper methods for common notification scenarios
     */

    @Async
    public void notifyContentPublished(Long userId, String contentTitle, Long contentId) {
        createNotification(CreateNotificationRequest.builder()
                .userId(userId)
                .type(NotificationType.CONTENT_PUBLISHED)
                .title("Content Published")
                .message("Your content '" + contentTitle + "' has been published!")
                .linkUrl("/contents/" + contentId)
                .entityType("CONTENT")
                .entityId(contentId)
                .priority(NotificationPriority.NORMAL)
                .build());
    }

    @Async
    public void notifyContentApproved(Long userId, String contentTitle, Long contentId) {
        createNotification(CreateNotificationRequest.builder()
                .userId(userId)
                .type(NotificationType.CONTENT_APPROVED)
                .title("Content Approved")
                .message("Your content '" + contentTitle + "' has been approved!")
                .linkUrl("/contents/" + contentId)
                .entityType("CONTENT")
                .entityId(contentId)
                .priority(NotificationPriority.HIGH)
                .build());
    }

    @Async
    public void notifyContentRejected(Long userId, String contentTitle, Long contentId, String reason) {
        createNotification(CreateNotificationRequest.builder()
                .userId(userId)
                .type(NotificationType.CONTENT_REJECTED)
                .title("Content Rejected")
                .message("Your content '" + contentTitle + "' has been rejected. Reason: " + reason)
                .linkUrl("/contents/" + contentId)
                .entityType("CONTENT")
                .entityId(contentId)
                .priority(NotificationPriority.HIGH)
                .build());
    }

    @Async
    public void notifyNewComment(Long userId, String commenterName, String contentTitle, Long contentId) {
        createNotification(CreateNotificationRequest.builder()
                .userId(userId)
                .type(NotificationType.CONTENT_COMMENTED)
                .title("New Comment")
                .message(commenterName + " commented on '" + contentTitle + "'")
                .linkUrl("/contents/" + contentId + "#comments")
                .entityType("CONTENT")
                .entityId(contentId)
                .priority(NotificationPriority.NORMAL)
                .build());
    }

    @Async
    public void notifyMention(Long userId, String mentionerName, String contentTitle, Long contentId) {
        createNotification(CreateNotificationRequest.builder()
                .userId(userId)
                .type(NotificationType.MENTION)
                .title("You were mentioned")
                .message(mentionerName + " mentioned you in '" + contentTitle + "'")
                .linkUrl("/contents/" + contentId)
                .entityType("CONTENT")
                .entityId(contentId)
                .priority(NotificationPriority.HIGH)
                .build());
    }

    /**
     * Map entity to DTO
     */
    private NotificationDTO mapToDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .linkUrl(notification.getLinkUrl())
                .entityType(notification.getEntityType())
                .entityId(notification.getEntityId())
                .priority(notification.getPriority())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    /**
     * Map preference entity to DTO
     */
    private NotificationPreferenceDTO mapPreferenceToDTO(NotificationPreference preference) {
        return NotificationPreferenceDTO.builder()
                .id(preference.getId())
                .userId(preference.getUser().getId())
                .emailEnabled(preference.getEmailEnabled())
                .inAppEnabled(preference.getInAppEnabled())
                .pushEnabled(preference.getPushEnabled())
                .contentUpdates(preference.getContentUpdates())
                .commentsEnabled(preference.getCommentsEnabled())
                .mentionsEnabled(preference.getMentionsEnabled())
                .workflowNotifications(preference.getWorkflowNotifications())
                .systemAlerts(preference.getSystemAlerts())
                .digestFrequency(preference.getDigestFrequency())
                .build();
    }
}
