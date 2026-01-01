package com.java.ppp.pppbackend.service;

import com.java.ppp.pppbackend.dto.*;
import com.java.ppp.pppbackend.entity.*;
import com.java.ppp.pppbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final DeliverableRepository deliverableRepository;
    private final TicketRepository ticketRepository;
    // private final TaskRepository taskRepository; // Assuming you have tasks

    public List<ProjectResponseDTO> getProjectsForUser(User user) {
        List<Project> projects;

        // --- 1. Determine Scope based on Role ---
        boolean isSuperAdmin = user.getRoles().stream().anyMatch(r -> r.getName().getDbValue().equalsIgnoreCase("super_admin"));
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().getDbValue().equalsIgnoreCase("admin"));

        if (isSuperAdmin) {
            // Super Admin: See ALL projects
            projects = projectRepository.findAll();
        } else if (isAdmin) {
            // Organization Admin: See ALL projects in their Organization
            if (user.getOrganization() != null) {
                projects = projectRepository.findByOrganizationId(user.getOrganization().getId());
            } else {
                projects = Collections.emptyList();
            }
        } else {
            // Consultants / Clients: See only ASSIGNED projects
            projects = projectRepository.findProjectsByUserId(user.getId());
        }

        // --- 2. Map to DTOs ---
        return projects.stream()
                .map(this::mapToProjectResponseDTO)
                .collect(Collectors.toList());
    }

    private ProjectResponseDTO mapToProjectResponseDTO(Project p) {
        // Fetch Stats (Ideally optimize this to batch queries for production)
        long totalDeliverables = deliverableRepository.countByProjectId(p.getId());
        long completedDeliverables = deliverableRepository.countByProjectIdAndStatus(p.getId(), "COMPLETED");

        long totalTickets = ticketRepository.countByProjectId(p.getId());
        long openTickets = ticketRepository.countByProjectIdAndStatus(p.getId(), TicketStatus.OPEN);

        // Placeholder for Tasks (if you don't have a Task entity yet)
        long totalTasks = 0;
        long completedTasks = 0;

        // Calculate Spent (Placeholder logic or sum from Expenses entity)
        BigDecimal spent = BigDecimal.ZERO;

        return ProjectResponseDTO.builder()
                .id(p.getId().toString())
                .title(p.getName()) // Assuming 'name' contains Arabic
                .titleEn(p.getName()) // Placeholder for English
                .description(p.getDescription())
                .descriptionEn(p.getDescription()) // Placeholder
                .status(p.getStatus().toString().toLowerCase()) // Enum to string
                .priority("medium") // Default if Priority field missing in Entity
                .progress(p.getProgress() != null ? p.getProgress() : 0)
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .budget(p.getBudget())
                .spent(spent)

                // --- Nested Objects ---
                .client(mapClientInfo(p))
                .consultant(mapConsultantInfo(p.getProjectManager()))
                .team(mapTeamMembers(p.getMembers()))

                // --- Stats ---
                .deliverables(totalDeliverables)
                .completedDeliverables(completedDeliverables)
                .tasks(totalTasks)
                .completedTasks(completedTasks)
                .tickets(totalTickets)
                .openTickets(openTickets)
                .build();
    }

    private ClientInfoDTO mapClientInfo(Project p) {
        if (p.getOrganization() == null) return null;
        return ClientInfoDTO.builder()
                .organization(p.getOrganization().getName())
                .organizationEn(p.getOrganization().getName()) // Placeholder
                .name("Admin Contact") // Ideally fetch from Org Contact Person
                .email("admin@org.com")
                .avatar("OR")
                .build();
    }

    private ConsultantInfoDTO mapConsultantInfo(User pm) {
        if (pm == null) return null;
        return ConsultantInfoDTO.builder()
                .name(pm.getFirstName() + " " + pm.getLastName())
                .role("Lead Consultant")
                .avatar(pm.getFirstName().substring(0, 1))
                .build();
    }

    private List<TeamMemberSummaryDTO> mapTeamMembers(List<ProjectMember> members) {
        if (members == null) return Collections.emptyList();
        return members.stream()
                .map(m -> TeamMemberSummaryDTO.builder()
                        .name(m.getUser().getFirstName() + " " + m.getUser().getLastName())
                        .role(m.getRole()) // Project Role (e.g., 'Specialist')
                        .email(m.getUser().getEmail())
                        .avatar(m.getUser().getFirstName().substring(0, 1))
                        .build())
                .limit(5) // Limit for UI performance
                .collect(Collectors.toList());
    }

    public List<DeliverableDTO> getProjectDeliverables(UUID projectId) {
        // Use the repository method we added earlier
        List<Deliverable> deliverables = deliverableRepository.findByProjectId(projectId);

        return deliverables.stream()
                .map(d -> DeliverableDTO.builder()
                        .id(d.getId())
                        .title(d.getTitle()) // or getTitle()
                        .status(d.getStatus()) // Ensure this matches your Enum/String
                        .createdAt(d.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}