package com.java.ppp.pppbackend.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ProjectResponseDTO {
    private String id;
    private String title;
    private String titleEn;
    private String description;
    private String descriptionEn;
    private String status;         // 'active', 'planning', etc.
    private String priority;       // 'high', 'medium', etc.
    private int progress;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal budget;
    private BigDecimal spent;      // Calculated field

    // Nested Objects
    private ClientInfoDTO client;
    private ConsultantInfoDTO consultant; // Usually the Project Manager
    private List<TeamMemberSummaryDTO> team;

    // Statistics
    private long deliverables;
    private long completedDeliverables;
    private long tasks;
    private long completedTasks;
    private long tickets;
    private long openTickets;
}
