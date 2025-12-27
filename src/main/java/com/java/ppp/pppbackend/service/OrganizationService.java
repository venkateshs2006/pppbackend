package com.java.ppp.pppbackend.service;

import com.java.ppp.pppbackend.dto.OrganizationDTO;
import com.java.ppp.pppbackend.entity.Organization;
import com.java.ppp.pppbackend.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public List<OrganizationDTO> getAllOrganizations() {
        return organizationRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
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
                .createdAt(org.getCreatedAt())
                .updatedAt(org.getUpdatedAt())
                .build();
    }
}