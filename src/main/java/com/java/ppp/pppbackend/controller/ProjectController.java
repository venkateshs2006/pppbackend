package com.java.ppp.pppbackend.controller;

import com.java.ppp.pppbackend.dto.*;
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
    public ResponseEntity<List<DeliverableDTO>> getProjectDeliverables(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProjectDeliverables(id));
    }
}