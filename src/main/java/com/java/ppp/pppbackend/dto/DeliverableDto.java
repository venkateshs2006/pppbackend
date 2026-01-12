package com.java.ppp.pppbackend.dto;

import com.java.ppp.pppbackend.entity.DeliverableStatus;
import com.java.ppp.pppbackend.entity.DeliverableType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliverableDto {
    private UUID id;
    private String title;
    private String titleEn;
    private String description;
    private String descriptionEn;
    private DeliverableType type;
    private DeliverableStatus status;
    private String version;
    private UUID parentId;
    private UUID projectId;
    private String fileName;
    private String fileUrl;
    private Long assignedToId;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}