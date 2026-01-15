package com.java.ppp.pppbackend.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportsDashboardDTO {
    private OverviewDTO overview;
    private List<ProjectProgressDTO> projectProgress;
    private List<DeliverableStatusStatsDTO> deliverablesByStatus;
    private List<TicketPriorityStatsDTO> ticketsByPriority;
    private List<MonthlyTrendDTO> monthlyTrends;
    private List<ClientMetricDTO> clientMetrics;

    @Data
    @Builder
    public static class OverviewDTO {
        private long totalProjects;
        private long activeProjects;
        private long completedProjects;
        private long totalDeliverables;
        private long approvedDeliverables;
        private long pendingDeliverables;
        private long totalTickets;
        private long resolvedTickets;
        private long openTickets;
        private BigDecimal totalRevenue;
        private BigDecimal collectedRevenue;
        private double clientSatisfaction;
    }

    @Data
    @Builder
    public static class ProjectProgressDTO {
        private UUID id;
        private String name;
        private String nameEn;
        private int progress;
        private BigDecimal budget;
        private BigDecimal spent;
    }

    @Data
    @Builder
    public static class DeliverableStatusStatsDTO {
        private String status;
        private long count;
        private double percentage;
    }

    @Data
    @Builder
    public static class TicketPriorityStatsDTO {
        private String priority;
        private long count;
        private double percentage;
    }

    @Data
    @Builder
    public static class MonthlyTrendDTO {
        private String month;
        private String monthEn;
        private long projects;
        private long deliverables;
        private long tickets;
    }

    @Data
    @Builder
    public static class ClientMetricDTO {
        private Long clientId;
        private String name;
        private String nameEn;
        private int satisfaction;
        private long projects;
        private BigDecimal value;
    }
}