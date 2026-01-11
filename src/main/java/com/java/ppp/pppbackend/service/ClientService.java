package com.java.ppp.pppbackend.service;

import com.java.ppp.pppbackend.dto.ClientDTO;
import com.java.ppp.pppbackend.dto.ClientDashboardStatsDTO;
import com.java.ppp.pppbackend.entity.*;
import com.java.ppp.pppbackend.exception.ResourceNotFoundException;
import com.java.ppp.pppbackend.repository.ClientRepository;

import com.java.ppp.pppbackend.repository.ProjectRepository;
import com.java.ppp.pppbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.java.ppp.pppbackend.entity.Client;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    public List<ClientDTO> getAllclients() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<Client> clients;
        // 2. Check Role
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_SUPER_ADMIN"))) {
            // Admin: View ALL clients
            clients = clientRepository.findAll();

            return clientRepository.findAll().stream().map(org -> {
                ClientDTO dto = mapToDTO(org);
                long total = projectRepository.countByClientId(org.getId());
                long active = projectRepository.countByClientIdAndStatus(org.getId(), ProjectStatus.ACTIVE);
                dto.setProjectsCount(total);
                dto.setActiveProjectsCount(active);
                return dto;
            }).collect(Collectors.toList());
        } else {
            // Standard User: View only clients linked to projects they are members of
            List<Project> projects=projectRepository.findProjectsByUserId(currentUser.getId());
            return getclientsFromProjects(projects);
        }
    }

    public ClientDTO getClient(Long id) {
        Client org = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        return mapToDTO(org);
    }

    public ClientDTO createClient(ClientDTO dto) {
        Client org = Client.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .logoUrl(dto.getLogoUrl())
                .stripeCustomerId(dto.getStripeCustomerId())
                .stripeCustomerName(dto.getContactPersonName())
                .subscriptionPlan(dto.getSubscriptionPlan())
                .subscriptionStatus(dto.getSubscriptionStatus())
                .stripeCustomerId(dto.getStripeCustomerId())
                .build();
        return mapToDTO(clientRepository.save(org));
    }

    public ClientDTO updateClient(Long id, ClientDTO dto) {
        Client org = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        org.setName(dto.getName());
        org.setDescription(dto.getDescription());
        org.setSubscriptionPlan(dto.getSubscriptionPlan());
        org.setSubscriptionStatus(dto.getSubscriptionStatus());
        org.setLogoUrl(dto.getLogoUrl());
        org.setStripeCustomerId(dto.getContactEmail());
        org.setStripeCustomerName(dto.getContactPersonName());
        org.setSubscriptionPlan(dto.getSubscriptionPlan());
        org.setSubscriptionStatus(dto.getSubscriptionStatus());
        return mapToDTO(clientRepository.save(org));
    }
    public ClientDashboardStatsDTO getDashboardStats() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<Client> clients;

          // 2. Check Role
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_SUPER_ADMIN"))) {
            // Fetch raw data
            long totalClients = clientRepository.count();
            long activeClients = clientRepository.countBySubscriptionStatus(SubscriptionStatus.ACTIVE);
            long totalProjects = projectRepository.count();
            Double avgSatisfaction = clientRepository.getAverageSatisfaction();

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
            long totalClients = clientRepository.countUserClients(currentUser.getId());
            long activeClients = clientRepository.countUserActiveClients(currentUser.getId(),SubscriptionStatus.ACTIVE);
            long totalProjects = projectRepository.countUserProjects(currentUser.getId());
            Double avgSatisfaction = clientRepository.getUserAverageSatisfaction(currentUser.getId());

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
    public void deleteClient(Long id) {
        clientRepository.deleteById(id);
    }

    private ClientDTO mapToDTO(Client org) {
        return ClientDTO.builder()
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
    public List<ClientDTO> getclientsFromProjects(List<Project> projects) {
        return projects.stream()
                // 1. Extract the Organization object from each Project
                .map(Project::getClient)

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