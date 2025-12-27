package com.java.ppp.pppbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {
    private String type;
    private Object payload;
    private Long timestamp;

    public static WebSocketMessage notification(NotificationDTO notification) {
        return WebSocketMessage.builder()
                .type("NOTIFICATION")
                .payload(notification)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static WebSocketMessage unreadCount(Long count) {
        return WebSocketMessage.builder()
                .type("UNREAD_COUNT")
                .payload(count)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}