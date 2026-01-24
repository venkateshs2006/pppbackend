package com.java.ppp.pppbackend.service;

import com.java.ppp.pppbackend.dto.ReportsDashboardDTO;
import com.java.ppp.pppbackend.entity.*;
import com.java.ppp.pppbackend.exception.ResourceNotFoundException;
import com.java.ppp.pppbackend.repository.*;
import com.java.ppp.pppbackend.security.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportsService {

    private final ProjectRepository projectRepository;
    private final DeliverableRepository deliverableRepository;
    private final TicketRepository ticketRepository;
    private final ClientRepository clientRepository;
    private final SecurityContext securityContext;
    private final UserRepository userRepository;
    public ReportsDashboardDTO getDashboardData(String period, String projectIdFilter) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 1. Fetch Projects accessible to User
        List<Project> projects = fetchAccessibleProjects(currentUser);

        // 2. Apply UI Filter (Specific Project)
        if (!"all".equalsIgnoreCase(projectIdFilter)) {
            try {
                UUID pId = UUID.fromString(projectIdFilter);
                projects = projects.stream()
                        .filter(p -> p.getId().equals(pId))
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                // Handle invalid UUID if necessary
            }
        }

        List<UUID> projectIds = projects.stream().map(Project::getId).collect(Collectors.toList());

        if (projectIds.isEmpty()) {
            return new ReportsDashboardDTO();
        }

        return ReportsDashboardDTO.builder()
                .overview(buildOverview(projects, projectIds))
                .projectProgress(buildProjectProgress(projects))
                .deliverablesByStatus(buildDeliverableStats(projectIds))
                .ticketsByPriority(buildTicketStats(projectIds))
                .monthlyTrends(buildMonthlyTrends(projectIds))
                .clientMetrics(buildClientMetrics(currentUser))
                .build();
    }

    private ReportsDashboardDTO.OverviewDTO buildOverview(List<Project> projects, List<UUID> projectIds) {
        long totalProjects = projects.size();
        long activeProjects = projects.stream().filter(p -> "ACTIVE".equals(p.getStatus().name())).count();
        long completedProjects = projects.stream().filter(p -> "COMPLETED".equals(p.getStatus().name())).count();

        long totalDeliverables = deliverableRepository.countByProjectIdIn(projectIds);
        long pendingDeliverables = deliverableRepository.countByStatusAndProjectIdIn(DeliverableStatus.REVIEW, projectIds); // Check your DeliverableStatus enum name
        long approvedDeliverables = totalDeliverables - pendingDeliverables; // Simplified

        long totalTickets = ticketRepository.countByProjectIdIn(projectIds);
        long resolvedTickets = ticketRepository.countByStatusAndProjectIdIn(TicketStatus.RESOLVED, projectIds);
        long openTickets = totalTickets - resolvedTickets;

        BigDecimal totalRevenue = projects.stream()
                .map(p -> p.getBudget() != null ? p.getBudget() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal spent = projects.stream()
                .map(p -> p.getSpent() != null ? p.getSpent() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ReportsDashboardDTO.OverviewDTO.builder()
                .totalProjects(totalProjects)
                .activeProjects(activeProjects)
                .completedProjects(completedProjects)
                .totalDeliverables(totalDeliverables)
                .approvedDeliverables(approvedDeliverables)
                .pendingDeliverables(pendingDeliverables)
                .totalTickets(totalTickets)
                .resolvedTickets(resolvedTickets)
                .openTickets(openTickets)
                .totalRevenue(totalRevenue)
                .collectedRevenue(spent)
                .clientSatisfaction(90.0)
                .build();
    }

    private List<ReportsDashboardDTO.ProjectProgressDTO> buildProjectProgress(List<Project> projects) {
        return projects.stream().map(p -> ReportsDashboardDTO.ProjectProgressDTO.builder()
                .id(p.getId())
                .name(p.getTitleAr())
                .nameEn(p.getTitleEn())
                .progress(p.getProgress() != null ? p.getProgress() : 0)
                .budget(p.getBudget() != null ? p.getBudget() : BigDecimal.ZERO)
                .spent(p.getSpent() != null ? p.getSpent() : BigDecimal.ZERO)
                .build()).collect(Collectors.toList());
    }

    private List<ReportsDashboardDTO.DeliverableStatusStatsDTO> buildDeliverableStats(List<UUID> projectIds) {
        List<Object[]> results = deliverableRepository.countByStatusForProjects(projectIds);
        long total = results.stream().mapToLong(row -> (long) row[1]).sum();

        return results.stream().map(row -> {
            String status = row[0].toString();
            long count = (long) row[1];
            double percentage = total > 0 ? (double) count / total * 100 : 0;
            return ReportsDashboardDTO.DeliverableStatusStatsDTO.builder()
                    .status(status)
                    .count(count)
                    .percentage(Math.round(percentage))
                    .build();
        }).collect(Collectors.toList());
    }

    private List<ReportsDashboardDTO.TicketPriorityStatsDTO> buildTicketStats(List<UUID> projectIds) {
        List<Object[]> results = ticketRepository.countByPriorityForProjects(projectIds);
        long total = results.stream().mapToLong(row -> (long) row[1]).sum();

        return results.stream().map(row -> {
            String priority = row[0].toString();
            long count = (long) row[1];
            double percentage = total > 0 ? (double) count / total * 100 : 0;
            return ReportsDashboardDTO.TicketPriorityStatsDTO.builder()
                    .priority(priority)
                    .count(count)
                    .percentage(Math.round(percentage))
                    .build();
        }).collect(Collectors.toList());
    }

//    private List<ReportsDashboardDTO.MonthlyTrendDTO> buildMonthlyTrends(List<UUID> projectIds) {
//        // Placeholder for monthly trends - implement actual logic if needed
//        List<ReportsDashboardDTO.MonthlyTrendDTO> trends = new ArrayList<>();
//        LocalDateTime now = LocalDateTime.now();
//        for (int i = 5; i >= 0; i--) {
//            LocalDateTime date = now.minusMonths(i);
//            trends.add(ReportsDashboardDTO.MonthlyTrendDTO.builder()
//                    .month(date.getMonth().getDisplayName(TextStyle.FULL, new Locale("ar")))
//                    .monthEn(date.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
//                    .projects(new Random().nextInt(5))
//                    .deliverables(new Random().nextInt(15))
//                    .tickets(new Random().nextInt(10))
//                    .build());
//        }
//        return trends;
//    }

    private List<ReportsDashboardDTO.MonthlyTrendDTO> buildMonthlyTrends(List<UUID> projectIds) {
        List<ReportsDashboardDTO.MonthlyTrendDTO> trends = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();

        // Loop for the last 6 months (including current)
        for (int i = 5; i >= 0; i--) {
            YearMonth targetMonth = currentMonth.minusMonths(i);

            // Calculate Start and End of the month
            LocalDateTime startDateTime = targetMonth.atDay(1).atStartOfDay();
            LocalDateTime endDateTime = targetMonth.atEndOfMonth().atTime(LocalTime.MAX);

            // Convert to Timestamp for Entities that use java.sql.Timestamp (Ticket, Deliverable)
            Timestamp startTimestamp = Timestamp.valueOf(startDateTime);
            Timestamp endTimestamp = Timestamp.valueOf(endDateTime);

            // 1. Fetch Actual Data
            // Projects: Count created in this month
            long newProjects = projectRepository.countCreatedBetween(projectIds, startDateTime, endDateTime);

            // 2. Deliverables: ✅ FIX - Pass LocalDateTime (Entity uses LocalDateTime)
            long completedDeliverables = deliverableRepository.countByStatusAndDate(
                    projectIds,
                    DeliverableStatus.APPROVED,
                    startDateTime, // Passing LocalDateTime
                    endDateTime    // Passing LocalDateTime
            );

            // 3. Tickets: ✅ FIX - Pass Timestamp (Entity uses Timestamp)
            long resolvedTickets = ticketRepository.countByStatusAndDate(
                    projectIds,
                    TicketStatus.RESOLVED,
                    startTimestamp, // Passing Timestamp
                    endTimestamp    // Passing Timestamp
            );



            // 2. Build DTO
            trends.add(ReportsDashboardDTO.MonthlyTrendDTO.builder()
                    .month(targetMonth.getMonth().getDisplayName(TextStyle.FULL, new Locale("ar")))
                    .monthEn(targetMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                    .projects(newProjects)
                    .deliverables(completedDeliverables)
                    .tickets(resolvedTickets)
                    .build());
        }
        return trends;
    }


    private List<ReportsDashboardDTO.ClientMetricDTO> buildClientMetrics(User user) {
        // Return metrics only for authorized roles
        return new ArrayList<>();
    }

    private List<Project> fetchAccessibleProjects(User user) {
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equals(RoleType.SUPER_ADMIN));
        if (isAdmin) {
            return projectRepository.findAll();
        }
        return projectRepository.findProjectsByUserId(user.getId());
    }
}