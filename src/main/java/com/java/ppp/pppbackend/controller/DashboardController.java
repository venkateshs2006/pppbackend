package com.java.ppp.pppbackend.controller;

import com.java.ppp.pppbackend.dto.DashboardResponseDTO;
import com.java.ppp.pppbackend.entity.User;
import com.java.ppp.pppbackend.exception.ResourceNotFoundException;
import com.java.ppp.pppbackend.repository.UserRepository;
import com.java.ppp.pppbackend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    @Autowired
    private DashboardService dashboardService;
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<DashboardResponseDTO> getDashboardData() {
        // 1. Get Logged in User email/username from Security Context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 2. Fetch full user entity (including Role and Organization)
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        System.out.println("User Details :"+currentUser.toString());
        // 3. Generate Dashboard Data
        DashboardResponseDTO data = dashboardService.getDashboardData(currentUser);

        return ResponseEntity.ok(data);
    }
}
