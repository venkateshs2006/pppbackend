package com.java.ppp.pppbackend.service;


import com.java.ppp.pppbackend.dto.TicketCommentDTO;
import com.java.ppp.pppbackend.dto.TicketDTO;
import com.java.ppp.pppbackend.entity.*;
import com.java.ppp.pppbackend.exception.BadRequestException;
import com.java.ppp.pppbackend.exception.ResourceNotFoundException;
import com.java.ppp.pppbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
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
    @Autowired
    private ProjectRepository projectRepository;
    // --- Ticket Operations ---
    private static final Set<String> ALLOWED_APPROVER_ROLES = Set.of("admin", "lead_consultant");
    public TicketDTO createTicket(TicketDTO dto) {
        Ticket ticket = new Ticket();
// 1. Copy simple fields (title, description, category, etc.)
        // We exclude complex fields to handle them manually
        BeanUtils.copyProperties(dto, ticket, "project", "assignedTo", "status", "priority");

        // 2. Handle Enums (Status & Priority)
        // DTO has them as Enums now, so we can set them directly if not null
        ticket.setStatus(dto.getStatus() != null ? dto.getStatus() : TicketStatus.OPEN);
        ticket.setPriority(dto.getPriority() != null ? dto.getPriority() : TicketPriority.MEDIUM);

        // 3. Handle Project Relationship
        if (dto.getProject() != null && dto.getProject().getId() != null) {
            Project project = projectRepository.findById(dto.getProject().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + dto.getProject().getId()));
            ticket.setProject(project);
        }

        // 4. Handle Assigned User Relationship
        if (dto.getAssignedTo() != null && dto.getAssignedTo().getId() != null) {
            try {
                Long userId = Long.parseLong(dto.getAssignedTo().getId());
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
                ticket.setAssignedTo(user);
            } catch (NumberFormatException e) {
                throw new BadRequestException("Invalid User ID format: " + dto.getAssignedTo().getId());
            }
        }

        // 5. Save Entity
        ticket = ticketRepository.save(ticket);

        // 6. Convert saved Entity back to DTO
        // (Don't use BeanUtils here either for complex types, reuse your mapper or set manually)
        return mapToDTO(ticket);
    }

    public List<TicketDTO> getTicketsByProject(UUID projectId) {
        return ticketRepository.findByProjectId(projectId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // --- Comment Operations ---

    @Transactional
    public TicketDTO addComment(UUID ticketId, TicketCommentDTO dto) {
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
        Ticket newticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        return mapToDTO(newticket);
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
        if (!TicketStatus.FOR_REVIEW.equals(ticket.getStatus())) {
            throw new RuntimeException("Ticket is not ready for review. Status is: " + ticket.getStatus());
        }

        // 3. Approve -> Mark Resolved
        ticket.setStatus(TicketStatus.RESOLVED);
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
        ticket.setStatus(TicketStatus.FOR_REVIEW);
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

    /**
     * Get all tickets relevant to a user (Assigned OR Created by them)
     */
    public List<TicketDTO> getTicketsByUser(Long userId) {
        return ticketRepository.findByAssignedToIdOrCreatedById(userId, userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    public TicketDTO getTicketById(UUID id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with ID: " + id));
        return convertToDTO(ticket);
    }

    public List<TicketDTO> getAllTickets(String status, String priority) {
        return ticketRepository.findAll().stream().map(tickets -> {
            TicketDTO dto = mapToDTO(tickets);
            return dto;
        }).collect(Collectors.toList());
    }
    // --- Mapper (The FIX is here) ---

    private TicketDTO mapToDTO(Ticket ticket) {
        TicketDTO dto = new TicketDTO();

        // 1. Basic Fields
        dto.setId(ticket.getId());
        dto.setTitle(ticket.getTitle());
        dto.setTitleEn(ticket.getTitle()); // Or specific field
        dto.setDescription(ticket.getDescription());
        dto.setDescriptionEn(ticket.getDescription()); // Or specific field

        dto.setStatus(ticket.getStatus() != null ? ticket.getStatus() : TicketStatus.OPEN);
        dto.setPriority(ticket.getPriority() != null ? ticket.getPriority() : TicketPriority.MEDIUM);

        dto.setCategory(ticket.getCategory());
        dto.setCategoryEn(ticket.getCategory());

        if (ticket.getCreatedAt() != null) dto.setCreatedAt(ticket.getCreatedAt().toString());
        if (ticket.getUpdatedAt() != null) dto.setUpdatedAt(ticket.getUpdatedAt().toString());
        if (ticket.getDueDate() != null) dto.setDueDate(ticket.getDueDate().toString());

        // 2. Map Project
        if (ticket.getProject() != null) {
            TicketDTO.ProjectInfo projectInfo = TicketDTO.ProjectInfo.builder()
                    .id(ticket.getProject().getId())
                    .name(ticket.getProject().getTitleAr())
                    .nameEn(ticket.getProject().getTitleEn())
                    .build();
            dto.setProject(projectInfo);

            // Map Client from Project
            Client org = ticket.getProject().getClient();
            if (org != null) {
                TicketDTO.ClientInfo clientInfo = TicketDTO.ClientInfo.builder()
                        .name(org.getName())
                        .organization(org.getName())
                        .avatar(org.getName().substring(0, 1).toUpperCase())
                        .build();
                dto.setClient(clientInfo);
            }
        }

        // 3. Map Assigned User (SAFE MAPPING)
        if (ticket.getAssignedTo() != null) {
            User user = ticket.getAssignedTo();
            String fullName = (user.getFirstName() != null ? user.getFirstName() : "") + " " + (user.getLastName() != null ? user.getLastName() : "");
            String initial = (user.getFirstName() != null && !user.getFirstName().isEmpty()) ? user.getFirstName().substring(0, 1) : "U";

            // ✅ FIX: Safe Role Access (Avoids .get() on empty Optional)
            String roleName = "Member"; // Default
            if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                roleName = user.getRoles().iterator().next().getName().toString();
            }

            TicketDTO.AssignedInfo assignedInfo = TicketDTO.AssignedInfo.builder()
                    .id(String.valueOf(user.getId()))
                    .name(fullName.trim())
                    .role(roleName)
                    .avatar(initial)
                    .build();
            dto.setAssignedTo(assignedInfo);
        }

        // 4. Map Responses
        if (ticket.getResponses() != null) {
            List<TicketDTO.TicketResponseDTO> responses = ticket.getResponses().stream().map(res -> {
                TicketDTO.TicketResponseDTO.TicketResponseDTOBuilder responseBuilder = TicketDTO.TicketResponseDTO.builder()
                        .id(String.valueOf(res.getId()))
                        .message(res.getComment())
                        .timestamp(res.getCreatedAt().toString());

                // Fetch User info manually if Comment has userId
                if (res.getUserId() != null) {
                    userRepository.findById(res.getUserId()).ifPresent(commentAuthor -> {
                        String authorName = commentAuthor.getFirstName() + " " + commentAuthor.getLastName();
                        String authorRole = (commentAuthor.getRoles() != null && !commentAuthor.getRoles().isEmpty())
                                ? commentAuthor.getRoles().iterator().next().getName().toString()
                                : "User";

                        responseBuilder.author(TicketDTO.TicketResponseDTO.ResponseAuthor.builder()
                                .name(authorName)
                                .role(authorRole)
                                .avatar(commentAuthor.getFirstName().substring(0,1))
                                .build());
                    });
                }

                return responseBuilder.build();
            }).collect(Collectors.toList());
            dto.setResponses(responses);
        }

        return dto;
    }


}