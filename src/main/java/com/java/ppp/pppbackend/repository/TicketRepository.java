package com.java.ppp.pppbackend.repository;

import com.java.ppp.pppbackend.entity.Ticket;
import com.java.ppp.pppbackend.entity.TicketStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    long countByStatus(String status);
    long countByStatusNot(String status); // For 'open' tickets (assuming open != closed)
    long countByStatus(TicketStatus status);
    List<Ticket> findByCreatedByIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    List<Ticket> findByAssignedToId(Long userId);
    List<Ticket> findByCreatedById(Long userId);
    List<Ticket> findByAssignedToIdOrCreatedById(Long assignedToId, Long createdById);
    List<Ticket> findByProjectId(UUID projectId);
}