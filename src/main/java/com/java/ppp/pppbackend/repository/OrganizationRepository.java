package com.java.ppp.pppbackend.repository;

import com.java.ppp.pppbackend.entity.Organization;
import com.java.ppp.pppbackend.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    // Find an organization by its exact name
    Optional<Organization> findByName(String name);

    // Find all organizations with a specific subscription status (e.g., ACTIVE)
    List<Organization> findBySubscriptionStatus(SubscriptionStatus subscriptionStatus);

    // Check if an organization exists by name (useful for validation before creation)
    boolean existsByName(String name);

    // Find organization by Stripe Customer ID (useful for billing webhooks)
    Optional<Organization> findByStripeCustomerId(String stripeCustomerId);
}