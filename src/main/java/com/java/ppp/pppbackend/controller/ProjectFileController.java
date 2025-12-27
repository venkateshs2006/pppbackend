package com.java.ppp.pppbackend.controller;

import com.java.ppp.pppbackend.dto.ProjectFileDTO;
import com.java.ppp.pppbackend.service.ProjectFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/files")
@RequiredArgsConstructor
public class ProjectFileController {

    private final ProjectFileService fileService;

    @GetMapping
    public ResponseEntity<List<ProjectFileDTO>> getFiles(@PathVariable UUID projectId) {
        return ResponseEntity.ok(fileService.getFilesByProject(projectId));
    }

    @PostMapping
    public ResponseEntity<ProjectFileDTO> uploadFile(
            @PathVariable Long projectId,
            @RequestBody ProjectFileDTO fileDTO) {
        fileDTO.setId(projectId);
        return ResponseEntity.ok(fileService.addFile(fileDTO));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long fileId) {
        fileService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }
}