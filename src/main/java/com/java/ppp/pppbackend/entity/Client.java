package com.java.ppp.pppbackend.entity;

import com.java.ppp.pppbackend.entity.SubscriptionPlan;
import com.java.ppp.pppbackend.entity.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "clients")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url")
    private String logoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan", nullable = false)
    @Builder.Default
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.BASIC;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_status")
    @Builder.Default
    private SubscriptionStatus subscriptionStatus = SubscriptionStatus.ACTIVE;

    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    @Column(name = "stripe_customer_name")
    private String stripeCustomerName;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "satisfaction")
    private Integer satisfaction;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "total_revenue_value")
    private BigDecimal totalRevenueValue;
}