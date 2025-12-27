package com.java.ppp.pppbackend.entity;

import com.java.ppp.pppbackend.entity.SubscriptionPlan;
import com.java.ppp.pppbackend.entity.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "organizations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Organization {

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

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}