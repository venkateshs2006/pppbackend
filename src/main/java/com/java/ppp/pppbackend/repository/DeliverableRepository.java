package com.java.ppp.pppbackend.repository;

import com.java.ppp.pppbackend.entity.Deliverable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DeliverableRepository extends JpaRepository<Deliverable, UUID> {

    // Used for dashboard stats (e.g., "Pending Approvals")
    long countByStatus(String status);

    // Used to fetch recent deliverables for the dashboard list
    @Query("SELECT d FROM Deliverable d JOIN FETCH d.project p ORDER BY d.updatedAt DESC")
    List<Deliverable> findRecentDeliverables(Pageable pageable);

    // Fetch deliverables for a specific user (if needed for Sub-Consultant view)
    @Query("SELECT d FROM Deliverable d WHERE d.createdBy.id = :userId OR d.project.projectManager.id = :userId")
    List<Deliverable> findByMemberId(@Param("userId") Long userId, Pageable pageable);

    List<Deliverable> findByProjectIdOrderByOrderIndexAsc(UUID projectId);
    List<Deliverable> findByParentId(UUID parentId);

}