package com.java.ppp.pppbackend.repository;

import com.java.ppp.pppbackend.entity.Project;
import com.java.ppp.pppbackend.entity.ProjectStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    // General stats
    long countByStatus(String status);
    // CHANGE: Use ProjectStatus enum, NOT String
    long countByStatus(ProjectStatus status);

    @Query("SELECT p FROM Project p WHERE p.projectManager.id = :userId")
    List<Project> findByMemberId(@Param("userId") Long userId, Pageable pageable);
    // Role-based Queries
    List<Project> findByOrganizationIdOrderByUpdatedAtDesc(String orgId, Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.projectManager.id = :userId")
    List<Project> findByMemberIdOrderByUpdatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    // Calculate average progress
    @Query("SELECT AVG(p.progress) FROM Project p WHERE p.organization.id = :orgId")
    Double getAverageProgressByOrganization(@Param("orgId") String orgId);

    List<Project> findByProjectManagerId(Long projectManagerId);
    // 1. For Organization Admins: Fetch all projects in their specific Org
    List<Project> findByOrganizationId(Long organizationId);

    // 2. For Consultants/Clients: Fetch projects where they are a member
    // Assumes you have a ProjectMember entity linking Project and User
    @Query("SELECT p FROM Project p JOIN p.members m WHERE m.user.id = :userId")
    List<Project> findProjectsByUserId(@Param("userId") Long userId);
}