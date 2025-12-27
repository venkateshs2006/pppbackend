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
    // CHANGE: Use TicketStatus enum, NOT String
    long countByStatus(TicketStatus status);
    List<Ticket> findByCreatedByIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    List<Ticket> findByProjectId(UUID projectId);
    List<Ticket> findByAssignedToId(Long userId);
    }