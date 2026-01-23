package com.java.ppp.pppbackend.service;

import com.java.ppp.pppbackend.dto.RoleDTO;
import com.java.ppp.pppbackend.entity.Role;
import com.java.ppp.pppbackend.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private RoleDTO mapToDTO(Role role) {
        return RoleDTO.builder()
                .id(role.getId())
                .name(role.getName().name()) // Enum to String
                .description(role.getDescription())
                .build();
    }
}