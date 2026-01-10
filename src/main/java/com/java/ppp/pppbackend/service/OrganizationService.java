package com.java.ppp.pppbackend.service;

import com.java.ppp.pppbackend.dto.ClientDashboardStatsDTO;
import com.java.ppp.pppbackend.dto.OrganizationDTO;
import com.java.ppp.pppbackend.entity.*;
import com.java.ppp.pppbackend.exception.ResourceNotFoundException;
import com.java.ppp.pppbackend.repository.OrganizationRepository;
import com.java.ppp.pppbackend.repository.ProjectRepository;
import com.java.ppp.pppbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.CharSetUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    public List<OrganizationDTO> getAllOrganizations() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<Organization> organizations;
        // 2. Check Role
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_SUPER_ADMIN"))) {
            // Admin: View ALL Organizations
            organizations = organizationRepository.findAll();

            return organizationRepository.findAll().stream().map(org -> {
                OrganizationDTO dto = mapToDTO(org);
                long total = projectRepository.countByOrganizationId(org.getId());
                long active = projectRepository.countByOrganizationIdAndStatus(org.getId(), ProjectStatus.ACTIVE);
                dto.setProjectsCount(total);
                dto.setActiveProjectsCount(active);
                return dto;
            }).collect(Collectors.toList());
        } else {
            // Standard User: View only organizations linked to projects they are members of
            List<Project> projects=projectRepository.findProjectsByUserId(currentUser.getId());
            return getOrganizationsFromProjects(projects);
        }
    }

    public OrganizationDTO getOrganization(Long id) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        return mapToDTO(org);
    }

    public OrganizationDTO createOrganization(OrganizationDTO dto) {
        Organization org = Organization.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .logoUrl(dto.getLogoUrl())
                .subscriptionPlan(dto.getSubscriptionPlan())
                .subscriptionStatus(dto.getSubscriptionStatus())
                .stripeCustomerId(dto.getStripeCustomerId())
                .build();
        return mapToDTO(organizationRepository.save(org));
    }

    public OrganizationDTO updateOrganization(Long id, OrganizationDTO dto) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        org.setName(dto.getName());
        org.setDescription(dto.getDescription());
        org.setSubscriptionPlan(dto.getSubscriptionPlan());
        org.setSubscriptionStatus(dto.getSubscriptionStatus());

        return mapToDTO(organizationRepository.save(org));
    }
    public ClientDashboardStatsDTO getDashboardStats() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<Organization> organizations;

          // 2. Check Role
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_SUPER_ADMIN"))) {
            // Fetch raw data
            long totalClients = organizationRepository.count();
            long activeClients = organizationRepository.countBySubscriptionStatus(SubscriptionStatus.ACTIVE);
            long totalProjects = projectRepository.count();
            Double avgSatisfaction = organizationRepository.getAverageSatisfaction();

            // Round satisfaction to integer or 1 decimal place if preferred
            double formattedSatisfaction = Math.round(avgSatisfaction * 10.0) / 10.0;
            BigDecimal revenue = projectRepository.calculateTotalBudget();

            return ClientDashboardStatsDTO.builder()
                    .totalClients(totalClients)
                    .activeClients(activeClients)
                    .totalProjects(totalProjects)
                    .averageSatisfaction(formattedSatisfaction)
                    .revenueCollected(revenue)
                    .build();
        } else {
            long totalClients = organizationRepository.countUserOrganizations(currentUser.getId());
            long activeClients = organizationRepository.countUserActiveOrganizations(currentUser.getId(),SubscriptionStatus.ACTIVE);
            long totalProjects = projectRepository.countUserProjects(currentUser.getId());
            Double avgSatisfaction = organizationRepository.getUserAverageSatisfaction(currentUser.getId());

            // Round satisfaction to integer or 1 decimal place if preferred
            double formattedSatisfaction = Math.round(avgSatisfaction * 10.0) / 10.0;
            BigDecimal revenue = projectRepository.calculateTotalBudget();

            return ClientDashboardStatsDTO.builder()
                    .totalClients(totalClients)
                    .activeClients(activeClients)
                    .totalProjects(totalProjects)
                    .averageSatisfaction(formattedSatisfaction)
                    .revenueCollected(revenue)
                    .build();
        }

    }
    public void deleteOrganization(Long id) {
        organizationRepository.deleteById(id);
    }

    private OrganizationDTO mapToDTO(Organization org) {
        return OrganizationDTO.builder()
                .id(org.getId())
                .name(org.getName())
                .description(org.getDescription())
                .logoUrl(org.getLogoUrl())
                .subscriptionPlan(org.getSubscriptionPlan())
                .subscriptionStatus(org.getSubscriptionStatus())
                .stripeCustomerId(org.getStripeCustomerId())
                .contactEmail(org.getStripeCustomerId())
                .contactPersonName(org.getStripeCustomerId()!=null?org.getStripeCustomerId().replaceAll("@.*","").replaceAll("[^a-zA-Z]+", " ").trim():"")
                .createdAt(org.getCreatedAt())
                .updatedAt(org.getUpdatedAt())
                .build();
    }
    public List<OrganizationDTO> getOrganizationsFromProjects(List<Project> projects) {
        return projects.stream()
                // 1. Extract the Organization object from each Project
                .map(Project::getOrganization)

                // 2. Remove nulls (safety check in case a project has no org)
                .filter(Objects::nonNull)

                // 3. Remove duplicates (e.g., if User is on 3 projects for "Alpha Corp", show "Alpha Corp" once)
                .distinct()

                // 4. Convert to DTO (using your helper method)
                .map(this::mapToDTO)

                // 5. Collect into a final List
                .collect(Collectors.toList());
    }


}