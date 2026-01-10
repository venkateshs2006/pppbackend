package com.java.ppp.pppbackend.dto;

import com.java.ppp.pppbackend.entity.ProjectStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ProjectDTO {
    private UUID id;
    private Long organizationId;
    private String name;
    private ProjectStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal budget;
    private BigDecimal spent;
    private Integer progress;
    private Long projectManagerId;
    private String projectManagerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String title;
    private String titleEn;
    private String description;
    private String descriptionEn;
    private String priority;
    private String clientId; // To link organization
}