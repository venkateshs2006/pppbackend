package com.java.ppp.pppbackend.controller;


import com.java.ppp.pppbackend.dto.DeliverableDto;
import com.java.ppp.pppbackend.service.DeliverableService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/deliverables")
@RequiredArgsConstructor
public class DeliverableController {

    private final DeliverableService service;

    @PostMapping
    public ResponseEntity<DeliverableDto> create(@RequestBody DeliverableDto dto) {
        return ResponseEntity.ok(service.createDeliverable(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeliverableDto> update(@PathVariable UUID id, @RequestBody DeliverableDto dto) {
        return ResponseEntity.ok(service.updateDeliverable(id, dto));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<DeliverableDto>> getByProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(service.getByProject(projectId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliverableDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deleteDeliverable(id);
        return ResponseEntity.ok().build();
    }

    // Upload File Endpoint
    @PostMapping("/{id}/upload")
    public ResponseEntity<DeliverableDto> uploadFile(@PathVariable UUID id, @RequestParam("file") MultipartFile file) throws IOException {
        // Implement file storage logic (S3/Local) inside service and return updated DTO
        // service.uploadFile(id, file);
        DeliverableDto updatedDeliverable = service.uploadFile(id, file);
        return ResponseEntity.ok(updatedDeliverable);
    }

    // Action: Submit for Review
    @PutMapping("/{id}/submit")
    public ResponseEntity<Void> submitForReview(@PathVariable UUID id, @RequestParam Long clientId) {
        service.submitForApproval(id, clientId);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = service.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // Default to binary if type cannot be determined
        }

        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}