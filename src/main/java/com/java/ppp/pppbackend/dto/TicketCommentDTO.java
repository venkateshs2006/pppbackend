package com.java.ppp.pppbackend.dto;


import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class TicketCommentDTO {
    private UUID id;
    private UUID ticketId;
    private Long userId;
    private String name;
    private String role;
    private String avatar;
    private String comment;
    private Boolean isInternal;
    private OffsetDateTime createdAt;
}