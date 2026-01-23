package com.java.ppp.pppbackend.controller;

import com.java.ppp.pppbackend.dto.RoleDTO;
import com.java.ppp.pppbackend.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "API for fetching system roles")
public class RoleController {

    private final RoleService roleService;

    @Operation(
            summary = "Get All Roles",
            description = "Retrieve list of all available roles",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        // You can restrict this if needed, e.g., @PreAuthorize("hasRole('ADMIN')")
        // Currently allowing authenticated users to fetch roles for dropdowns
        return ResponseEntity.ok(roleService.getAllRoles());
    }
}