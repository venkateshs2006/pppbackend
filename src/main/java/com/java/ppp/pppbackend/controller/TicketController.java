package com.java.ppp.pppbackend.controller;

import com.java.ppp.pppbackend.dto.TicketCommentDTO;
import com.java.ppp.pppbackend.dto.TicketDTO;
import com.java.ppp.pppbackend.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "Tickets", description = "Ticket and Support Management API")
public class TicketController {
    @Autowired
    private TicketService ticketService;
    @GetMapping
    public ResponseEntity<List<TicketDTO>> getAllTickets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority
    ) {
        return ResponseEntity.ok(ticketService.getAllTickets(status, priority));
    }
    @PostMapping
    @Operation(summary = "Create a new Ticket")
    public ResponseEntity<TicketDTO> createTicket(@RequestBody TicketDTO ticketDTO) {
        return ResponseEntity.ok(ticketService.createTicket(ticketDTO));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get tickets by Project")
    public ResponseEntity<List<TicketDTO>> getTicketsByProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(ticketService.getTicketsByProject(projectId));
    }

    // --- Comments Endpoints ---

    @PostMapping("/{ticketId}/comments")
    @Operation(summary = "Add a comment to a ticket")
    public ResponseEntity<TicketDTO> addComment(
            @PathVariable UUID ticketId,
            @RequestBody TicketCommentDTO commentDTO) {
        return ResponseEntity.ok(ticketService.addComment(ticketId, commentDTO));
    }

    @GetMapping("/{ticketId}/comments")
    @Operation(summary = "Get comments for a ticket")
    public ResponseEntity<List<TicketCommentDTO>> getComments(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(ticketService.getComments(ticketId));
    }


    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject ticket", description = "Moves ticket to Redo state.")
    public ResponseEntity<TicketDTO> rejectTicket(
            @PathVariable UUID id,
            @RequestParam Long approverId) {
        return ResponseEntity.ok(ticketService.rejectTicket(id, approverId));
    }

    @PatchMapping("/{id}/assign")
    @Operation(summary = "Reassign Ticket", description = "Allows Admins, Leads, or Owner to reassign.")
    public ResponseEntity<TicketDTO> reassignTicket(
            @PathVariable UUID id,
            @RequestParam Long newAssigneeId,
            @RequestParam Long actorId) { // In real app, actorId comes from JWT
        return ResponseEntity.ok(ticketService.reassignTicket(id, newAssigneeId, actorId));
    }

    @PostMapping("/{id}/attachments")
    @Operation(summary = "Upload File to Ticket")
    public ResponseEntity<String> uploadAttachment(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            @RequestParam Long uploaderId) {

        // 1. Upload file to Storage Service (S3/Local) -> Get URL
        String fileUrl = "http://storage.com/" + file.getOriginalFilename(); // Mock URL

        // 2. Save metadata
        ticketService.addAttachment(id, uploaderId, file.getOriginalFilename(), fileUrl, file.getContentType());

        return ResponseEntity.ok("File uploaded successfully");
    }

    @PatchMapping("/{id}/submit-approval")
    @Operation(summary = "Submit for Client Approval", description = "Moves ticket to 'pending_client_approval'.")
    public ResponseEntity<TicketDTO> submitForApproval(@PathVariable UUID id) {
        return ResponseEntity.ok(ticketService.submitForApproval(id));
    }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "Client Approval", description = "Main Client approves -> Ticket Closed.")
    public ResponseEntity<TicketDTO> approveTicket(
            @PathVariable UUID id,
            @RequestParam Long clientId) {
        return ResponseEntity.ok(ticketService.clientApprove(id, clientId));
    }
    // 1. Get by Ticket ID (UUID)
    // Regex ensures this only triggers for valid UUID strings
    @GetMapping("/{id}")
    @Operation(summary = "Get Ticket by UUID", description = "Retrieve a single ticket by its UUID")
    public ResponseEntity<TicketDTO> getTicketById(@PathVariable UUID id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    // 2. Get by User ID (Long/Integer)
    // Regex ensures this only triggers for numeric IDs
    @GetMapping("/userid/{userId}")
    @Operation(summary = "Get Tickets by User ID", description = "Retrieve all tickets assigned to or created by a specific User")
    public ResponseEntity<List<TicketDTO>> getTicketsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ticketService.getTicketsByUser(userId));
    }
}
