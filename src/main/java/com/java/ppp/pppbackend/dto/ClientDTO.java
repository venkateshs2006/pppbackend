package com.java.ppp.pppbackend.dto;

import com.java.ppp.pppbackend.entity.SubscriptionPlan;
import com.java.ppp.pppbackend.entity.SubscriptionStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ClientDTO {
    private Long id;
    private String name;
    private String description;
    private String logoUrl;
    private SubscriptionPlan subscriptionPlan;
    private SubscriptionStatus subscriptionStatus;
    private String stripeCustomerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String contactPersonName;
    private String contactEmail;
    private long projectsCount;
    private long activeProjectsCount;
}