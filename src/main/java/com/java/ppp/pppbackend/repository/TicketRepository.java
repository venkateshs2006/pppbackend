package com.java.ppp.pppbackend.repository;

import com.java.ppp.pppbackend.entity.Ticket;
import com.java.ppp.pppbackend.entity.TicketStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

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
    long countByStatusAndProjectIdIn(String status, List<UUID> projectIds);
    long countByStatus(TicketStatus status);
    long countByStatusAndProjectIdIn(TicketStatus status, List<UUID> projectIds);
    // Spring Data JPA automatically understands "ProjectId" maps to "project.id"
    long countByProjectId(UUID projectId);

    long countByProjectIdAndStatus(UUID projectId, TicketStatus status);

}