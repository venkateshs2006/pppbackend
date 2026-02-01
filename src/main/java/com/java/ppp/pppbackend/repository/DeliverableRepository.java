package com.java.ppp.pppbackend.repository;

import com.java.ppp.pppbackend.entity.Deliverable;
import com.java.ppp.pppbackend.entity.DeliverableStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface DeliverableRepository extends JpaRepository<Deliverable, UUID> {

    // Used for dashboard stats (e.g., "Pending Approvals")
    long countByStatus(DeliverableStatus status);

    // Used to fetch recent deliverables for the dashboard list
    @Query("SELECT d FROM Deliverable d JOIN FETCH d.project p ORDER BY d.updatedAt DESC")
    List<Deliverable> findRecentDeliverables(Pageable pageable);


    @Query("SELECT d FROM Deliverable d WHERE d.createdById = :userId OR d.project.projectManager.id = :userId")
    List<Deliverable> findByUserOrManager(@Param("userId") Long userId, Pageable pageable);
    List<Deliverable> findByProjectIdOrderByOrderIndexAsc(UUID projectId);

    List<Deliverable> findTop5ByOrderByUpdatedAtDesc();
    List<Deliverable> findTop5ByProjectIdInOrderByUpdatedAtDesc(List<UUID> projectIds);

    long countByStatusAndProjectIdIn(DeliverableStatus status, List<UUID> projectIds);


    // Count methods
    long countByProjectId(UUID projectId);
    long countByProjectIdIn(List<UUID> projectIds);

    // Check if your Deliverable entity uses String or Enum for status.
    // If String:
    long countByProjectIdAndStatus(UUID projectId, String status);
    // If Enum:
    // long countByProjectIdAndStatus(UUID projectId, DeliverableStatus status);
    List<Deliverable> findByProjectId(UUID projectId);

    List<Deliverable> findByProjectIdAndParentId(UUID projectId, UUID parentId);

    // Check if all deliverables for a project are closed
    boolean existsByProjectIdAndStatusNot(UUID projectId, DeliverableStatus status);

    @Query("SELECT d.status, COUNT(d) FROM Deliverable d WHERE d.project.id IN :projectIds GROUP BY d.status")
    List<Object[]> countByStatusForProjects(@Param("projectIds") List<UUID> projectIds);

    @Query("SELECT COUNT(d) FROM Deliverable d WHERE d.status = 'APPROVED' AND d.project.id IN :projectIds")
    long countApprovedForProjects(@Param("projectIds") List<UUID> projectIds);

    // ✅ FIX: Changed Timestamp to LocalDateTime to match Entity field 'updatedAt'
    @Query("SELECT COUNT(d) FROM Deliverable d WHERE d.project.id IN :projectIds AND d.status = :status AND d.updatedAt BETWEEN :startDate AND :endDate")
    long countByStatusAndDate(
            @Param("projectIds") List<UUID> projectIds,
            @Param("status") DeliverableStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COALESCE(SUM(d.weightage), 0) FROM Deliverable d WHERE d.project.id = :projectId AND d.status = :status")
    BigDecimal getWeightageByProjectAndStatus(@Param("projectId") UUID projectId, @Param("status") DeliverableStatus status);


    // Existing method for CREATE (sums everything)
    @Query("SELECT COALESCE(SUM(d.weightage), 0) FROM Deliverable d WHERE d.project.id = :projectId")
    BigDecimal getTotalWeightageByProject(@Param("projectId") UUID projectId);

    // ✅ NEW method for UPDATE (sums everything EXCEPT the specific ID)
    @Query("SELECT COALESCE(SUM(d.weightage), 0) FROM Deliverable d WHERE d.project.id = :projectId AND d.id != :excludeId")
    BigDecimal getTotalWeightageByProjectExcluding(@Param("projectId") UUID projectId, @Param("excludeId") UUID excludeId);
    @Transactional
    void deleteByProjectId(UUID projectId);
    List<Deliverable> findByProject_Id(UUID projectId);
}


