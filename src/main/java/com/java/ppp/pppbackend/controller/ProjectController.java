package com.java.ppp.pppbackend.controller;

import com.java.ppp.pppbackend.dto.ProjectDTO;
import com.java.ppp.pppbackend.dto.ProjectFileDTO;
import com.java.ppp.pppbackend.dto.ProjectMemberDTO;
import com.java.ppp.pppbackend.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectDTO> createProject(@RequestBody ProjectDTO projectDTO) {
        return ResponseEntity.ok(projectService.createProject(projectDTO));
    }

    @GetMapping
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO> getProject(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProject(id));
    }

    // --- Files ---

    @GetMapping("/{id}/files")
    public ResponseEntity<List<ProjectFileDTO>> getProjectFiles(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProjectFiles(id));
    }

    @PostMapping("/{id}/files")
    public ResponseEntity<ProjectFileDTO> addFile(@PathVariable UUID id, @RequestBody ProjectFileDTO fileDTO) {
        return ResponseEntity.ok(projectService.addFileToProject(id, fileDTO));
    }

    // --- Members ---

    @GetMapping("/{id}/members")
    public ResponseEntity<List<ProjectMemberDTO>> getProjectMembers(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProjectMembers(id));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<ProjectMemberDTO> addMember(@PathVariable UUID id, @RequestBody ProjectMemberDTO memberDTO) {
        return ResponseEntity.ok(projectService.addMember(id, memberDTO));
    }
}