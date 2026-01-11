package com.java.ppp.pppbackend.repository;

import com.java.ppp.pppbackend.entity.Client;
import com.java.ppp.pppbackend.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    // Find an organization by its exact name
    Optional<Client> findByName(String name);

    // Find all organizations with a specific subscription status (e.g., ACTIVE)
    List<Client> findBySubscriptionStatus(SubscriptionStatus subscriptionStatus);

    // Check if an organization exists by name (useful for validation before creation)
    boolean existsByName(String name);

    // Find organization by Stripe Customer ID (useful for billing webhooks)
    Optional<Client> findByStripeCustomerId(String stripeCustomerId);

    // ✅ Add this line to fix the error
    //long countBySubscriptionStatus(String status);
    long countBySubscriptionStatus(SubscriptionStatus status);
    // Your existing custom query
    @Query("SELECT COALESCE(AVG(o.satisfaction), 0) FROM Client o")
    Double getAverageSatisfaction();

    @Query("SELECT DISTINCT pm.project.client " +
            "FROM ProjectMember pm " +
            "WHERE pm.user.id = :userId " +
            "AND pm.project.client IS NOT NULL")
    List<Client> findClientsByMemberId(@Param("userId") Long userId);

    // --- USER METHODS (Filtered by ProjectMember) ---

    // 1. Total Clients (User's Assigned)
    @Query("SELECT COUNT(DISTINCT pm.project.client) FROM ProjectMember pm WHERE pm.user.id = :userId")
    long countUserClients(@Param("userId") Long userId);

    // 2. Active Clients (User's Assigned)
    @Query("SELECT COUNT(DISTINCT pm.project.client) FROM ProjectMember pm " +
            "WHERE pm.user.id = :userId AND pm.project.client.subscriptionStatus = :status")
    long countUserActiveClients(@Param("userId") Long userId, @Param("status") SubscriptionStatus status);

    // 4. Avg Satisfaction (User's Assigned)
    // Calculates average of the 'satisfaction' column for organizations the user works with
    @Query("SELECT COALESCE(AVG(pm.project.client.satisfaction), 0.0) " +
            "FROM ProjectMember pm WHERE pm.user.id = :userId")
    Double getUserAverageSatisfaction(@Param("userId") Long userId);
}