package com.java.ppp.pppbackend.controller;


import com.java.ppp.pppbackend.dto.DeliverableDTO;
import com.java.ppp.pppbackend.service.DeliverableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DeliverableController {

    private final DeliverableService deliverableService;

    // Get all deliverables for a specific project
//    @GetMapping("/projects/{projectId}/deliverables")
//    public ResponseEntity<List<DeliverableDTO>> getProjectDeliverables(@PathVariable UUID projectId) {
//        return ResponseEntity.ok(deliverableService.getDeliverablesByProject(projectId));
//    }

    // Create a new deliverable under a project
    @PostMapping("/projects/{projectId}/deliverables")
    public ResponseEntity<DeliverableDTO> createDeliverable(
            @PathVariable UUID projectId,
            @RequestBody DeliverableDTO dto) {
        dto.setProjectId(projectId);
        return ResponseEntity.ok(deliverableService.createDeliverable(dto));
    }

    // Get specific deliverable details
    @GetMapping("/deliverables/{id}")
    public ResponseEntity<DeliverableDTO> getDeliverable(@PathVariable UUID id) {
        return ResponseEntity.ok(deliverableService.getDeliverable(id));
    }

    // Update deliverable
    @PutMapping("/deliverables/{id}")
    public ResponseEntity<DeliverableDTO> updateDeliverable(
            @PathVariable UUID id,
            @RequestBody DeliverableDTO dto) {
        return ResponseEntity.ok(deliverableService.updateDeliverable(id, dto));
    }

    // Delete deliverable
    @DeleteMapping("/deliverables/{id}")
    public ResponseEntity<Void> deleteDeliverable(@PathVariable UUID id) {
        deliverableService.deleteDeliverable(id);
        return ResponseEntity.noContent().build();
    }
}