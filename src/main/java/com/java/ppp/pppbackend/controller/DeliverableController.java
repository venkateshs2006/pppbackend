package com.java.ppp.pppbackend.controller;

import com.java.ppp.pppbackend.dto.DeliverableDTO;
import com.java.ppp.pppbackend.service.DeliverableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api") // Base path
@RequiredArgsConstructor
public class DeliverableController {

    private final DeliverableService deliverableService;

    // GET /api/projects/{projectId}/deliverables
    @GetMapping("/deliverables/project/{projectId}")
    public ResponseEntity<List<DeliverableDTO>> getByProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(deliverableService.getProjectDeliverables(projectId));
    }
    // Get /api/deliverables
    @GetMapping("/deliverables")
    public ResponseEntity<List<DeliverableDTO>> getAllDeliverable() {
        return ResponseEntity.ok(deliverableService.getProjectDeliverable());
    }
    // POST /api/deliverables
    @PostMapping("/deliverables")
    public ResponseEntity<DeliverableDTO> createDeliverable(@RequestBody DeliverableDTO dto) {
        return ResponseEntity.ok(deliverableService.createDeliverable(dto));
    }

    // PUT /api/deliverables/{id}
    @PutMapping("/deliverables/{id}")
    public ResponseEntity<DeliverableDTO> updateDeliverable(@PathVariable UUID id, @RequestBody DeliverableDTO dto) {
        return ResponseEntity.ok(deliverableService.updateDeliverable(id, dto));
    }

    // DELETE /api/deliverables/{id}
    @DeleteMapping("/deliverables/{id}")
    public ResponseEntity<Void> deleteDeliverable(@PathVariable UUID id) {
        deliverableService.deleteDeliverable(id);
        return ResponseEntity.noContent().build();
    }
}