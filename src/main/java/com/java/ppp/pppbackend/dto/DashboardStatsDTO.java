package com.java.ppp.pppbackend.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class DashboardStatsDTO {
    private long totalOrganization;
    private long totalProjects;
    private long activeProjects;
    private long completedTasks;
    private long pendingApprovals;
    private long openTickets;
    private double overallProgress;
}