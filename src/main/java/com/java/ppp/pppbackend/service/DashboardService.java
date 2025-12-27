package com.java.ppp.pppbackend.service;

import com.java.ppp.pppbackend.dto.*;
import com.java.ppp.pppbackend.entity.*;
import com.java.ppp.pppbackend.repository.DeliverableRepository;
import com.java.ppp.pppbackend.repository.ProjectRepository;

import com.java.ppp.pppbackend.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    @Autowired
    private ProjectRepository projectRepo;

    @Autowired
    private TicketRepository ticketRepo;
    @Autowired
    private DeliverableRepository deliverableRepo;

    public DashboardResponseDTO getDashboardData(User user) {
        RoleType role = user.getRoles().iterator().next().getName(); // Assuming User -> Role -> RoleType
        System.out.println("Dashboard Service started...");
        DashboardStatsDTO stats;
        List<RecentProjectDTO> projects;
        List<RecentDeliverableDTO> deliverables;
        List<RecentTicketDTO> tickets;
        System.out.println("Dashboard Service Variable assigned...");
        System.out.println(role);
        // 1. Logic Switch based on Role (matching Dashboard.tsx logic)
        if (role == RoleType.SUB_CONSULTANT) {
            stats = getSubConsultantStats(user);
            projects = getAssignedProjects(user);
            // ... fetch assigned deliverables/tickets
        } else if (role == RoleType.MAIN_CLIENT || role == RoleType.SUB_CLIENT) {
            stats = getClientStats(user);
            projects = getOrganizationProjects(user);
            // ... fetch org deliverables/tickets
        } else {
            // Admin / Lead Consultant / Super Admin
            System.out.println("Dashboard Service Else Conditions...");
            stats = getGlobalStats();
            projects = getGlobalProjects();
        }
        System.out.println("Dashboard Service Global stats ended...");
        // 2. Fetch Common Lists (Simplified for brevity - mapped from Entities)
        deliverables = new ArrayList<>(); // Implement repository calls similar to projects
        tickets = new ArrayList<>();

        // 3. Lead Consultant Specifics
        List<KPIDTO> kpis = null;
        List<DeadlineDTO> deadlines = null;
        System.out.println("Dashboard Service KPI started...");
        if (role == RoleType.LEAD_CONSULTANT) {
            kpis = generateKPIs();
            deadlines = generateDeadlines();
        }
        System.out.println("Dashboard Service Before ended...");
        return DashboardResponseDTO.builder()
                .stats(stats)
                .recentProjects(projects)
                .recentDeliverables(deliverables)
                .recentTickets(tickets)
                .kpis(kpis)
                .upcomingDeadlines(deadlines)
                .build();
    }

    // --- Helper Methods ---

    private DashboardStatsDTO getGlobalStats() {

        System.out.println("Project Repo :"+projectRepo.countByStatus(ProjectStatus.ACTIVE));
        System.out.println("Delivery Repo  :"+deliverableRepo.countByStatus("review"));
        System.out.println("Ticket Repo   :"+ticketRepo.countByStatus(TicketStatus.OPEN));
        System.out.println("Project Count  :"+projectRepo.count());


        DashboardStatsDTO data = DashboardStatsDTO.builder()
                .totalProjects(projectRepo.count())
                .activeProjects(projectRepo.countByStatus(ProjectStatus.ACTIVE))
                .pendingApprovals(deliverableRepo.countByStatus("review")) // Assuming 'review' = pending approval
                .openTickets(ticketRepo.countByStatus(TicketStatus.OPEN))
                .overallProgress(75.0) // Ideally calculate AVG from projects
                .build();
        System.out.println("Primary Data :"+data.toString());
        return data;
    }

    private DashboardStatsDTO getSubConsultantStats(User user) {
        // Only count tasks assigned to this consultant


        return DashboardStatsDTO.builder()
                .totalProjects(5) // Replace with actual Repo count query
                .activeProjects(3)
                .pendingApprovals(2)
                .openTickets(1)
                .overallProgress(60.0)
                .build();
    }

    private DashboardStatsDTO getClientStats(User user) {
        // Filter by Organization ID
        return DashboardStatsDTO.builder()
                .totalProjects(3) // Replace with repo.countByOrganizationId...
                .activeProjects(2)
                .completedTasks(12)
                .pendingApprovals(4)
                .openTickets(2)
                .overallProgress(45.0)
                .build();
    }

    private List<RecentProjectDTO> getGlobalProjects() {
        // Map Entities to DTOs
        return projectRepo.findAll(PageRequest.of(0, 5)).stream()
                .map(p -> RecentProjectDTO.builder()
                        .id(p.getId().toString())
                        .title(p.getName()) // Assuming DB has Arabic name
                        .titleEn(p.getName()) // You might need a separate column or translation table
                        .progress(p.getProgress())
                        .status(p.getStatus().toString())
                        .clientName(p.getOrganization().getName())
                        .dueDate(p.getEndDate())
                        .build())
                .toList();
    }

    private List<RecentProjectDTO> getAssignedProjects(User user) {
        return projectRepo.findByMemberIdOrderByUpdatedAtDesc(user.getId(), PageRequest.of(0, 5))
                .stream().map(this::mapToProjectDTO).toList();
    }

    private List<RecentProjectDTO> getOrganizationProjects(User user) {
        // Assuming User has getOrganizationId()
        // return projectRepo.findByOrganizationId...
        return new ArrayList<>();
    }

    private RecentProjectDTO mapToProjectDTO(com.java.ppp.pppbackend.entity.Project p) {
        return RecentProjectDTO.builder()
                .id(p.getId().toString())
                .title(p.getName())
                .progress(p.getProgress())
                .status(p.getStatus().toString())
                .clientName(p.getOrganization().getName())
                .dueDate(p.getEndDate())
                .build();
    }

    // Mocking logic for Lead Consultant specifics
    private List<KPIDTO> generateKPIs() {
        return List.of(
                KPIDTO.builder().label("Project Completion Rate").percentage(85).colorClass("bg-green-600").build(),
                KPIDTO.builder().label("Client Satisfaction").percentage(92).colorClass("bg-blue-600").build()
        );
    }

    private List<DeadlineDTO> generateDeadlines() {
        return List.of(
                DeadlineDTO.builder().title("Security Policy Review").projectName("Cybersecurity Project").daysRemaining(2).build()
        );
    }
}
