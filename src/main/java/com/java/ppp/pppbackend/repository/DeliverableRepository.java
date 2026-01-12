package com.java.ppp.pppbackend.repository;

import com.java.ppp.pppbackend.entity.Deliverable;
import com.java.ppp.pppbackend.entity.DeliverableStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    long countByStatusAndProjectIdIn(String status, List<UUID> projectIds);


    // Count methods
    long countByProjectId(UUID projectId);

    // Check if your Deliverable entity uses String or Enum for status.
    // If String:
    long countByProjectIdAndStatus(UUID projectId, String status);
    // If Enum:
    // long countByProjectIdAndStatus(UUID projectId, DeliverableStatus status);
    List<Deliverable> findByProjectId(UUID projectId);

    List<Deliverable> findByProjectIdAndParentId(UUID projectId, UUID parentId);

    // Check if all deliverables for a project are closed
    boolean existsByProjectIdAndStatusNot(UUID projectId, DeliverableStatus status);
}