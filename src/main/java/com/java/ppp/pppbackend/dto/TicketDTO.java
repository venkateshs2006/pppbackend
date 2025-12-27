package com.java.ppp.pppbackend.dto;


import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class TicketDTO {
    private UUID id;
    private UUID projectId;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String category;
    private OffsetDateTime dueDate;
    private Long assignedTo;
    private Long createdBy;
    private OffsetDateTime createdAt;
    private OffsetDateTime resolvedAt;
}
