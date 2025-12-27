package com.java.ppp.pppbackend.controller;

import com.java.ppp.pppbackend.dto.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    @MessageMapping("/notification.subscribe")
    @SendTo("/topic/notifications")
    public WebSocketMessage subscribe(@Payload WebSocketMessage message,
                                      SimpMessageHeaderAccessor headerAccessor) {
        String username = headerAccessor.getUser().getName();
        log.info("User {} subscribed to notifications", username);
        return message;
    }

    @MessageMapping("/notification.unsubscribe")
    public void unsubscribe(SimpMessageHeaderAccessor headerAccessor) {
        String username = headerAccessor.getUser().getName();
        log.info("User {} unsubscribed from notifications", username);
    }
}