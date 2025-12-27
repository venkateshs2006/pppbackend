package com.java.ppp.pppbackend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardResponseDTO {
    private DashboardStatsDTO stats;
    private List<RecentProjectDTO> recentProjects;
    private List<RecentDeliverableDTO> recentDeliverables;
    private List<RecentTicketDTO> recentTickets;

    // Specific for Lead Consultants
    private List<KPIDTO> kpis;
    private List<DeadlineDTO> upcomingDeadlines;
}