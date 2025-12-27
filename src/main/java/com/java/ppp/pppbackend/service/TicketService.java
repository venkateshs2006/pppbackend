package com.java.ppp.pppbackend.service;


import com.java.ppp.pppbackend.dto.TicketCommentDTO;
import com.java.ppp.pppbackend.dto.TicketDTO;
import com.java.ppp.pppbackend.entity.*;
import com.java.ppp.pppbackend.repository.TicketAttachmentRepository;
import com.java.ppp.pppbackend.repository.TicketCommentRepository;
import com.java.ppp.pppbackend.repository.TicketRepository;
import com.java.ppp.pppbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private TicketCommentRepository commentRepository;
    @Autowired
    private TicketAttachmentRepository ticketAttachmentRepository;
    @Autowired
    private UserRepository userRepository;
    // --- Ticket Operations ---
    private static final Set<String> ALLOWED_APPROVER_ROLES = Set.of("admin", "lead_consultant");
    public TicketDTO createTicket(TicketDTO dto) {
        Ticket ticket = new Ticket();
        BeanUtils.copyProperties(dto, ticket);
        ticket = ticketRepository.save(ticket);
        BeanUtils.copyProperties(ticket, dto);
        dto.setId(ticket.getId());
        return dto;
    }

    public List<TicketDTO> getTicketsByProject(UUID projectId) {
        return ticketRepository.findByProjectId(projectId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // --- Comment Operations ---

    @Transactional
    public TicketCommentDTO addComment(UUID ticketId, TicketCommentDTO dto) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        TicketComment comment = new TicketComment();
        comment.setTicket(ticket);
        comment.setUserId(dto.getUserId());
        comment.setComment(dto.getComment());
        comment.setIsInternal(dto.getIsInternal());

        comment = commentRepository.save(comment);

        TicketCommentDTO responseDTO = new TicketCommentDTO();
        BeanUtils.copyProperties(comment, responseDTO);
        responseDTO.setTicketId(ticket.getId());
        return responseDTO;
    }

    public List<TicketCommentDTO> getComments(UUID ticketId) {
        return commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .map(c -> {
                    TicketCommentDTO dto = new TicketCommentDTO();
                    BeanUtils.copyProperties(c, dto);
                    dto.setTicketId(c.getTicket().getId());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private TicketDTO convertToDTO(Ticket ticket) {
        TicketDTO dto = new TicketDTO();
        BeanUtils.copyProperties(ticket, dto);
        return dto;
    }

    @Transactional
    public TicketDTO approveTicket(UUID ticketId, Long approverId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // 1. Verify Approver Role (Iterating through Set)
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // LOGIC CHANGE: Check if ANY of the user's roles match the allowed roles.
        // .stream().anyMatch() stops iterating as soon as it finds 'true'.
        boolean isAuthorized = approver.getRoles().stream()
                .anyMatch(userRole -> ALLOWED_APPROVER_ROLES.contains(userRole.getDescription().toLowerCase()));

        /* Alternative "For-Loop" version if you prefer explicit iteration:

           boolean isAuthorized = false;
           for (String role : approver.getRoles()) {
               if ("admin".equalsIgnoreCase(role) || "lead_consultant".equalsIgnoreCase(role)) {
                   isAuthorized = true;
                   break; // Stop iterating immediately
               }
           }
        */

        if (!isAuthorized) {
            throw new RuntimeException("Unauthorized: Only Admin or Lead Consultant can approve.");
        }

        // 2. Verify State
        if (!TicketStatus.STATUS_FOR_REVIEW.equals(ticket.getStatus())) {
            throw new RuntimeException("Ticket is not ready for review. Status is: " + ticket.getStatus());
        }

        // 3. Approve -> Mark Resolved
        ticket.setStatus(TicketStatus.STATUS_RESOLVED);
        ticket.setResolvedAt(Timestamp.valueOf(LocalDateTime.now()));

        ticketRepository.save(ticket);
        return convertToDTO(ticket);
    }

    @Transactional
    public TicketDTO rejectTicket(UUID ticketId, Long approverId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));


        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Same authorization logic
        boolean isAuthorized = approver.getRoles().stream()
                .anyMatch(userRole -> ALLOWED_APPROVER_ROLES.contains(userRole.getDescription().toLowerCase()));

        if (!isAuthorized) {
            throw new RuntimeException("Unauthorized: Only Admin or Lead Consultant can reject.");
        }

        ticketRepository.save(ticket);
        return convertToDTO(ticket);
    }

    // Roles allowed to REASSIGN tickets
    private static final Set<String> MANAGER_ROLES = Set.of("super_admin", "admin", "lead_consultant");

    // --- 1. Reassign Ticket ---
    @Transactional
    public TicketDTO reassignTicket(UUID ticketId, Long newAssigneeId, Long actorId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new RuntimeException("Actor not found"));

        // Permission Check:
        // Allow if Actor is Admin/Lead OR Actor is the Ticket Owner (created_by)
        boolean isManager = actor.getRoles().stream()
                .anyMatch(r -> MANAGER_ROLES.contains(r.getName().getDbValue().toLowerCase()));

        boolean isOwner = ticket.getCreatedBy().equals(actorId);

        if (!isManager && !isOwner) {
            throw new RuntimeException("Unauthorized: You cannot reassign this ticket.");
        }

        // Validate New Assignee
        if (!userRepository.existsById(newAssigneeId)) {
            throw new RuntimeException("New assignee user not found");
        }

        ticket.setAssignedTo(userRepository.getById(newAssigneeId));
        ticket.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

        // Optional: Log this action in comments automatically

        return convertToDTO(ticketRepository.save(ticket));
    }

    // --- 2. Upload File ---
    @Transactional
    public void addAttachment(UUID ticketId, Long uploaderId, String fileName, String fileUrl, String fileType) {
        TicketAttachment attachment = TicketAttachment.builder()
                .ticketId(ticketId)
                .userId(uploaderId)
                .fileName(fileName)
                .fileUrl(fileUrl)
                .fileType(fileType)
                .build();
        ticketAttachmentRepository.save(attachment);
    }

    // --- 3. Submit for Approval ---
    @Transactional
    public TicketDTO submitForApproval(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // Move to Client Approval Status
        ticket.setStatus(TicketStatus.STATUS_FOR_REVIEW);
        ticket.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

        return convertToDTO(ticketRepository.save(ticket));
    }

    // --- 4. Main Client Approval ---
    @Transactional
    public TicketDTO clientApprove(UUID ticketId, Long clientId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Strict Check: Must be Main Client (or Super Admin)
        boolean isAuthorized = client.getRoles().contains("main_client") ||
                client.getRoles().contains("super_admin");

        if (!isAuthorized) {
            throw new RuntimeException("Unauthorized: Only Main Client can approve to close.");
        }

        // Final State
        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setResolvedAt(Timestamp.valueOf(LocalDateTime.now()));
        ticket.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

        return convertToDTO(ticketRepository.save(ticket));
    }




}