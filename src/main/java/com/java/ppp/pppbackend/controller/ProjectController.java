package com.java.ppp.pppbackend.controller;

import com.java.ppp.pppbackend.dto.*;
import com.java.ppp.pppbackend.entity.Role;
import com.java.ppp.pppbackend.entity.User;
import com.java.ppp.pppbackend.exception.ResourceNotFoundException;
import com.java.ppp.pppbackend.repository.UserRepository;
import com.java.ppp.pppbackend.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    @Autowired
    private ProjectService projectService;

       @Autowired
        private UserRepository userRepository;

        @GetMapping
        public ResponseEntity<List<ProjectResponseDTO>> getAllProjects() {
            // 1. Get Logged in User
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // 2. Get Projects based on Role logic inside Service
            List<ProjectResponseDTO> projects = projectService.getProjectsForUser(currentUser);

            return ResponseEntity.ok(projects);
        }


    @GetMapping("/{id}/deliverables")
    public ResponseEntity<List<DeliverableDto>> getProjectDeliverables(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProjectDeliverables(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> updateProject(
            @PathVariable UUID id,
            @RequestBody ProjectDTO projectDTO) {
        return ResponseEntity.ok(projectService.updateProject(id, projectDTO));
    }

    @PostMapping
    public ResponseEntity<ProjectResponseDTO> createProject(@RequestBody ProjectDTO projectDTO) {
        return ResponseEntity.ok(projectService.createProject(projectDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> getProject(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProject(id));
    }
    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getProjectMembers() {
        return ResponseEntity.ok(projectService.getAllProjectRole());
    }
    @GetMapping("/{id}/members")
    public ResponseEntity<List<TeamMemberSummaryDTO>> getProjectMembers(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProjectMembers(id));
    }
    @PostMapping("/{id}/members/{userId}/{role}/add")
    public ResponseEntity<TeamMemberSummaryDTO> addProjectMember(@PathVariable UUID id,@PathVariable Long userId, @PathVariable String role) {
        return ResponseEntity.ok(projectService.addProjectMember(id, userId, role));
    }
    @DeleteMapping("/{id}/members/{userId}/{role}/delete")
    public ResponseEntity<TeamMemberSummaryDTO> removeProjectMember(@PathVariable UUID id,@PathVariable Long userId, @PathVariable String role) {
        projectService.removeProjectMember(id, userId, role);
            return ResponseEntity.noContent().build();

    }
}