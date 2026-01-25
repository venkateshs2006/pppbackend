package com.java.ppp.pppbackend.repository;

import com.java.ppp.pppbackend.entity.Ticket;
import com.java.ppp.pppbackend.entity.TicketStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    long countByStatusNot(String status); // For 'open' tickets (assuming open != closed)
    List<Ticket> findByCreatedByIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    List<Ticket> findByAssignedToId(Long userId);
    List<Ticket> findByCreatedById(Long userId);
    List<Ticket> findByAssignedToIdOrCreatedById(Long assignedToId, Long createdById);
    List<Ticket> findByProjectId(UUID projectId);
    List<Ticket> findTop5ByOrderByCreatedAtDesc();
    List<Ticket> findTop5ByProjectIdInOrderByCreatedAtDesc(List<UUID> projectIds);

    long countByStatus(TicketStatus status);
    long countByStatusAndProjectIdIn(TicketStatus status, List<UUID> projectIds);
    // Spring Data JPA automatically understands "ProjectId" maps to "project.id"
    long countByProjectId(UUID projectId);

    long countByProjectIdAndStatus(UUID projectId, TicketStatus status);
    @Query("SELECT t.priority, COUNT(t) FROM Ticket t WHERE t.project.id IN :projectIds GROUP BY t.priority")
    List<Object[]> countByPriorityForProjects(@Param("projectIds") List<UUID> projectIds);

    @Query("SELECT t.status, COUNT(t) FROM Ticket t WHERE t.project.id IN :projectIds GROUP BY t.status")
    List<Object[]> countByStatusForProjects(@Param("projectIds") List<UUID> projectIds);

    long countByProjectIdIn(List<UUID> projectIds);

    // Inside interface TicketRepository
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.project.id IN :projectIds AND t.status = :status AND t.resolvedAt BETWEEN :startDate AND :endDate")
    long countByStatusAndDate(@Param("projectIds") List<UUID> projectIds, @Param("status") TicketStatus status, @Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

    // 2. Count OPEN tickets (Everything except CLOSED) for a project
    // Assuming 'CLOSED' is the status for closed tickets
    long countByProjectIdAndStatusNot(UUID projectId, TicketStatus status);

}