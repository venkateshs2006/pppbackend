package com.java.ppp.pppbackend.controller;


import com.java.ppp.pppbackend.dto.*;
import com.java.ppp.pppbackend.entity.NotificationType;
import com.java.ppp.pppbackend.entity.User;
import com.java.ppp.pppbackend.exception.UnauthorizedException;
import com.java.ppp.pppbackend.repository.UserRepository;
import com.java.ppp.pppbackend.security.SecurityContext;
import com.java.ppp.pppbackend.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management APIs")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "*")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SecurityContext securityContext;

    @PostMapping("/")
    @Operation(summary = "Create notification (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> createNotification(
            @Valid @RequestBody CreateNotificationRequest request) {
        notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "Notification created successfully"));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Create bulk notifications (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> createBulkNotification(
            @RequestParam List<Long> userIds,
            @Valid @RequestBody CreateNotificationRequest request) {
        notificationService.createBulkNotification(userIds, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "Bulk notifications created successfully"));
    }

    @GetMapping("/")
    @Operation(summary = "Get user notifications")
    public ResponseEntity<PageResponse<NotificationDTO>> getUserNotifications(
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = getCurrentUserId();
        Page<NotificationDTO> notifications = notificationService
                .getUserNotifications(userId, pageable);
        return ResponseEntity.ok(PageResponse.of(notifications));
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications")
    public ResponseEntity<PageResponse<NotificationDTO>> getUnreadNotifications(
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = getCurrentUserId();
        Page<NotificationDTO> notifications = notificationService
                .getUnreadNotifications(userId, pageable);
        return ResponseEntity.ok(PageResponse.of(notifications));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get notifications by type")
    public ResponseEntity<PageResponse<NotificationDTO>> getNotificationsByType(
            @PathVariable NotificationType type,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = getCurrentUserId();
        Page<NotificationDTO> notifications = notificationService
                .getNotificationsByType(userId, type, pageable);
        return ResponseEntity.ok(PageResponse.of(notifications));
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Get unread notifications count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        Long userId = getCurrentUserId();
        Long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<ApiResponse<String>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification marked as read"));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<String>> markAllAsRead() {
        Long userId = getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete notification")
    public ResponseEntity<ApiResponse<String>> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification deleted successfully"));
    }

    @GetMapping("/preferences")
    @Operation(summary = "Get notification preferences")
    public ResponseEntity<ApiResponse<NotificationPreferenceDTO>> getPreferences() {
        Long userId = getCurrentUserId();
        NotificationPreferenceDTO preferences = notificationService.getPreferences(userId);
        return ResponseEntity.ok(ApiResponse.success(preferences));
    }

    @PutMapping("/preferences")
    @Operation(summary = "Update notification preferences")
    public ResponseEntity<ApiResponse<NotificationPreferenceDTO>> updatePreferences(
            @Valid @RequestBody NotificationPreferenceDTO preferenceDTO) {
        Long userId = getCurrentUserId();
        NotificationPreferenceDTO updated = notificationService
                .updatePreferences(userId, preferenceDTO);
        return ResponseEntity.ok(ApiResponse.success(updated, "Preferences updated successfully"));
    }

    private Long getCurrentUserId() {
        String username = securityContext.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        return user.getId();
    }
}


