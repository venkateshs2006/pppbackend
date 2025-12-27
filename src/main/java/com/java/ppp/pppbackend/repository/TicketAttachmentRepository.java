package com.java.ppp.pppbackend.repository;


import com.java.ppp.pppbackend.entity.TicketAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TicketAttachmentRepository extends JpaRepository<TicketAttachment, UUID> {

    // Fetch all files uploaded for a specific ticket
    List<TicketAttachment> findByTicketId(UUID ticketId);

    // Optional: Find by uploader if needed
    List<TicketAttachment> findByUserId(Long userId);
}