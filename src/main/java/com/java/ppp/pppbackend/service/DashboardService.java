package com.java.ppp.pppbackend.service;

import com.java.ppp.pppbackend.dto.*;
import com.java.ppp.pppbackend.entity.*;
import com.java.ppp.pppbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final OrganizationRepository organizationRepository;
    private final DeliverableRepository deliverableRepository;
    private final TicketRepository ticketRepository;

    public DashboardResponseDTO getDashboardData(User user) {
        // 1. Determine User Role/Scope
        boolean isSuperAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().getDbValue().equalsIgnoreCase("super_admin"));

        // 2. Fetch Data Scope (Projects & IDs)
        List<Project> scopeProjects;
        if (isSuperAdmin) {
            scopeProjects = projectRepository.findAll();
        } else {
            scopeProjects = projectRepository.findProjectsByUserId(user.getId());
        }

        List<UUID> projectIds = scopeProjects.stream()
                .map(Project::getId)
                .collect(Collectors.toList());

        // 3. Calculate Stats
        DashboardStatsDTO stats = calculateStats(scopeProjects, isSuperAdmin, projectIds);

        // 4. Fetch & Map Recent Projects (Top 5)
        List<RecentProjectDTO> recentProjects = scopeProjects.stream()
                //.sorted(Comparator.comparing(Project::getUpdatedAt).reversed()) // Uncomment if you track updates
                .limit(5)
                .map(this::mapToRecentProjectDTO)
                .collect(Collectors.toList());

        // 5. Fetch & Map Recent Deliverables (Top 5)
        List<Deliverable> deliverablesRaw = isSuperAdmin
                ? deliverableRepository.findTop5ByOrderByUpdatedAtDesc()
                : deliverableRepository.findTop5ByProjectIdInOrderByUpdatedAtDesc(projectIds);

        List<RecentDeliverableDTO> recentDeliverables = deliverablesRaw.stream()
                .map(this::mapToRecentDeliverableDTO)
                .collect(Collectors.toList());

        // 6. Fetch & Map Recent Tickets (Top 5)
        List<Ticket> ticketsRaw = isSuperAdmin
                ? ticketRepository.findTop5ByOrderByCreatedAtDesc()
                : ticketRepository.findTop5ByProjectIdInOrderByCreatedAtDesc(projectIds);

        List<RecentTicketDTO> recentTickets = ticketsRaw.stream()
                .map(this::mapToRecentTicketDTO)
                .collect(Collectors.toList());

        // 7. Calculate KPIs & Deadlines (Only for specific roles if needed, otherwise generic)
        List<KPIDTO> kpis = calculateKPIs(stats);
        List<DeadlineDTO> upcomingDeadlines = calculateDeadlines(scopeProjects);

        return DashboardResponseDTO.builder()
                .stats(stats)
                .recentProjects(recentProjects)
                .recentDeliverables(recentDeliverables)
                .recentTickets(recentTickets)
                .kpis(kpis)
                .upcomingDeadlines(upcomingDeadlines)
                .build();
    }

    // =========================================================================
    // Helper Methods: Statistics Calculation
    // =========================================================================
    private DashboardStatsDTO calculateStats(List<Project> projects, boolean isSuperAdmin, List<UUID> projectIds) {
        long totalOrgs = isSuperAdmin
                ? organizationRepository.count()
                : projects.stream()
                .map(p -> p.getOrganization() != null ? p.getOrganization().getId() : null) // Get ID from the object
                .filter(Objects::nonNull) // Safety check to remove nulls
                .distinct()
                .count();

        long totalProjects = projects.size();
        long activeProjects = projects.stream().filter(p -> "ACTIVE".equalsIgnoreCase(p.getStatus().getDbValue())).count();

        // Safe division for progress
        double overallProgress = 0.0;
        if (!projects.isEmpty()) {
            double sum = projects.stream().mapToInt(p -> p.getProgress() != null ? p.getProgress() : 0).sum();
            overallProgress = sum / projects.size();
        }

        // Fetch counts based on scope
        long completedTasks;
        long pendingApprovals;
        long openTickets;

        if (isSuperAdmin || projectIds.isEmpty()) {
            if (projectIds.isEmpty() && !isSuperAdmin) {
                completedTasks = 0; pendingApprovals = 0; openTickets = 0;
            } else {
                // Assuming Deliverable status is also an Enum? If so, use DeliverableStatus.COMPLETED
                // If Deliverable status is a String, keep it as "COMPLETED".
                completedTasks = deliverableRepository.countByStatus("COMPLETED");
                pendingApprovals = deliverableRepository.countByStatus("PENDING_APPROVAL");

                // HERE IS THE FIX FOR THE 500 ERROR:
                openTickets = ticketRepository.countByStatus(TicketStatus.OPEN);
            }
        } else {
            completedTasks = deliverableRepository.countByStatusAndProjectIdIn("COMPLETED", projectIds);
            pendingApprovals = deliverableRepository.countByStatusAndProjectIdIn("PENDING_APPROVAL", projectIds);

            // HERE IS THE FIX FOR THE 500 ERROR:
            openTickets = ticketRepository.countByStatusAndProjectIdIn(TicketStatus.OPEN, projectIds);
        }

        return DashboardStatsDTO.builder()
                .totalOrganization(totalOrgs)
                .totalProjects(totalProjects)
                .activeProjects(activeProjects)
                .completedTasks(completedTasks)
                .pendingApprovals(pendingApprovals)
                .openTickets(openTickets)
                .overallProgress(Math.round(overallProgress * 100.0) / 100.0)
                .build();
    }

    // =========================================================================
    // Helper Methods: Mappers
    // =========================================================================

    private RecentProjectDTO mapToRecentProjectDTO(Project p) {
        // Assuming your Organization Entity has a name, otherwise placeholders
        String clientName = p.getOrganization() != null ? p.getOrganization().getName() : "Unknown Client";

        return RecentProjectDTO.builder()
                .id(p.getId().toString())
                .title(p.getName())       // Assuming 'name' is Arabic/Default
                .titleEn(p.getName())     // Using same for EN if separate field missing
                .progress(p.getProgress() != null ? p.getProgress() : 0)
                .status(p.getStatus().getDbValue())
                .clientName(clientName)
                .clientNameEn(clientName)
                .dueDate(p.getEndDate())
                .build();
    }

    private RecentDeliverableDTO mapToRecentDeliverableDTO(Deliverable d) {
        return RecentDeliverableDTO.builder()
                .id(d.getId().toString())
                .title(d.getTitle())
                .titleEn(d.getTitle())
                .type("Document") // Placeholder or d.getType()
                .status(d.getStatus())
                .projectName(d.getProject().getName())
                .projectNameEn(d.getProject().getName())
                .version(d.getVersion() != null ? "v" + d.getVersion() : "v1.0")
                .build();
    }

    private RecentTicketDTO mapToRecentTicketDTO(Ticket t) {
        return RecentTicketDTO.builder()
                .id(t.getId().toString())
                .title(t.getTitle())
                .titleEn(t.getTitle())
                .priority(t.getPriority().name()) // Assuming Enum
                .status(t.getStatus().getDbValue())
                .projectName(t.getProject().getName())
                .projectNameEn(t.getProject().getName())
                .createdAt(t.getCreatedAt().toLocalDate())
                .build();
    }

    // =========================================================================
    // Helper Methods: KPIs & Deadlines
    // =========================================================================

    private List<KPIDTO> calculateKPIs(DashboardStatsDTO stats) {
        // Example Logic: Return Completion Rate & Active Project Rate
        List<KPIDTO> kpis = new ArrayList<>();

        // KPI 1: Project Completion Rate (Overall Progress)
        kpis.add(KPIDTO.builder()
                .label("Overall Progress")
                .labelEn("Overall Progress")
                .percentage((int) stats.getOverallProgress())
                .colorClass("bg-blue-600")
                .build());

        // KPI 2: Task Completion Rate (Example calculation)
        long totalTasks = stats.getCompletedTasks() + stats.getPendingApprovals(); // Simplified denominator
        int taskRate = totalTasks > 0 ? (int) ((stats.getCompletedTasks() * 100) / totalTasks) : 0;

        kpis.add(KPIDTO.builder()
                .label("Task Completion")
                .labelEn("Task Completion")
                .percentage(taskRate)
                .colorClass("bg-green-600")
                .build());

        return kpis;
    }

    private List<DeadlineDTO> calculateDeadlines(List<Project> projects) {
        // Filter projects ending in next 30 days
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysLater = today.plusDays(30);

        return projects.stream()
                .filter(p -> p.getEndDate() != null &&
                        !p.getEndDate().isBefore(today) &&
                        p.getEndDate().isBefore(thirtyDaysLater))
                .sorted(Comparator.comparing(Project::getEndDate))
                .limit(5)
                .map(p -> DeadlineDTO.builder()
                        .title("Project Due: " + p.getName())
                        .titleEn("Project Due: " + p.getName())
                        .projectName(p.getName())
                        .projectNameEn(p.getName())
                        .daysRemaining((int) ChronoUnit.DAYS.between(today, p.getEndDate()))
                        .build())
                .collect(Collectors.toList());
    }
}