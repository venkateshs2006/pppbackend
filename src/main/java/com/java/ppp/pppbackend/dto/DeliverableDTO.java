package com.java.ppp.pppbackend.dto;


import com.java.ppp.pppbackend.entity.DeliverableType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class DeliverableDTO {
    private UUID id;
    private UUID projectId;
    private UUID parentId;
    private String title;
    private String description;
    private DeliverableType type;
    private String status;
    private String version;
    private Integer orderIndex;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
