package com.java.ppp.pppbackend.entity;


import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "email_enabled")
    @Builder.Default
    private Boolean emailEnabled = true;

    @Column(name = "in_app_enabled")
    @Builder.Default
    private Boolean inAppEnabled = true;

    @Column(name = "push_enabled")
    @Builder.Default
    private Boolean pushEnabled = true;

    @Column(name = "content_updates")
    @Builder.Default
    private Boolean contentUpdates = true;

    @Column(name = "comments_enabled")
    @Builder.Default
    private Boolean commentsEnabled = true;

    @Column(name = "mentions_enabled")
    @Builder.Default
    private Boolean mentionsEnabled = true;

    @Column(name = "workflow_notifications")
    @Builder.Default
    private Boolean workflowNotifications = true;

    @Column(name = "system_alerts")
    @Builder.Default
    private Boolean systemAlerts = true;

    @Column(name = "digest_frequency", length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DigestFrequency digestFrequency = DigestFrequency.DAILY;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;
}
