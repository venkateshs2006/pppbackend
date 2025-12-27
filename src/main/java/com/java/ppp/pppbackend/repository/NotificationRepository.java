package com.java.ppp.pppbackend.repository;

import com.java.ppp.pppbackend.entity.Notification;
import com.java.ppp.pppbackend.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(
            Long userId, Boolean isRead, Pageable pageable);

    Page<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(
            Long userId, NotificationType type, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = false")
    Long countUnreadNotifications(Long userId);

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.isRead = false " +
            "ORDER BY n.priority DESC, n.createdAt DESC")
    List<Notification> findUnreadNotifications(Long userId, Pageable pageable);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.id = :id")
    void markAsRead(Long id, LocalDateTime readAt);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt " +
            "WHERE n.user.id = :userId AND n.isRead = false")
    void markAllAsRead(Long userId, LocalDateTime readAt);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.expiresAt < :now")
    void deleteExpiredNotifications(LocalDateTime now);

    List<Notification> findByIsSentFalseAndCreatedAtBefore(LocalDateTime before);
}
