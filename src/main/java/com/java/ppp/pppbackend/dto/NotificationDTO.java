package com.java.ppp.pppbackend.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.java.ppp.pppbackend.entity.NotificationPriority;
import com.java.ppp.pppbackend.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationDTO {
    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private String linkUrl;
    private String entityType;
    private Long entityId;
    private NotificationPriority priority;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
