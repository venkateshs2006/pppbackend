package com.java.ppp.pppbackend.dto;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class DeliverableDTO {
    private UUID id;
    private String title;
    private String description;
    private String type;       // "guide", "policy", etc.
    private String status;     // "DRAFT", "COMPLETED"
    private String version;
    private Integer orderIndex;

    // Relationship IDs
    private UUID projectId;  // UUID as String
    private Long createdById;  // User ID as Long
    private String createdByName; // Optional: To show who created it

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}