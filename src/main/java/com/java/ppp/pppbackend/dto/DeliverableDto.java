package com.java.ppp.pppbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.java.ppp.pppbackend.entity.DeliverableStatus;
import com.java.ppp.pppbackend.entity.DeliverableType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DeliverableDto {
    private UUID id;
    private String title;
    private String titleEn;
    private String description;
    private String descriptionEn;
    private DeliverableType type;
    private DeliverableStatus status;
    private String rejectionReason;
    private String version;
    private UUID parentId;
    private UUID projectId;
    private String fileName;
    @JsonProperty("weightage")
    private BigDecimal weightAge;
    private String fileUrl;
    private Long assignedToId;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}