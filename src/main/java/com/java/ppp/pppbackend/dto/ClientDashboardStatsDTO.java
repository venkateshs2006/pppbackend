package com.java.ppp.pppbackend.dto;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ClientDashboardStatsDTO {
    private long totalClients;
    private long activeClients;
    private long totalProjects;
    private double averageSatisfaction;
    private BigDecimal revenueCollected;
}