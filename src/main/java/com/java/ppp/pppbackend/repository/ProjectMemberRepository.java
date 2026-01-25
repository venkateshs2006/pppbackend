package com.java.ppp.pppbackend.repository;

import com.java.ppp.pppbackend.entity.ProjectMember;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {
    List<ProjectMember> findByProjectId(UUID projectId);

    Optional<ProjectMember> findByProjectIdAndUserId(UUID projectId, Long userId);
    @Modifying
    @Transactional // Required for update/delete operations
    @Query("UPDATE ProjectMember pm SET pm.role = :role WHERE pm.project.id = :projectId AND pm.user.id = :userId")
    void updateMemberRole(@Param("projectId") UUID projectId,
                          @Param("userId") Long userId,
                          @Param("role") String role);
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.user.id = :userId and pm.role = :role")
    ProjectMember findProjectMemberDetails(@Param("projectId") UUID projectId,
                             @Param("userId") Long userId,
                             @Param("role") String role);
    // Optional: Find all members for a specific project ID
    List<ProjectMember> findByUserId(Long userId);
    boolean existsByProjectIdAndRole(UUID projectId, String role);
}
