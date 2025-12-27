package com.java.ppp.pppbackend.service;

import com.java.ppp.pppbackend.dto.DeliverableDTO;
import com.java.ppp.pppbackend.entity.Deliverable;
import com.java.ppp.pppbackend.entity.Project;
import com.java.ppp.pppbackend.entity.User;
import com.java.ppp.pppbackend.repository.DeliverableRepository;
import com.java.ppp.pppbackend.repository.ProjectRepository;
import com.java.ppp.pppbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliverableService {

    private final DeliverableRepository deliverableRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public List<DeliverableDTO> getDeliverablesByProject(UUID projectId) {
        return deliverableRepository.findByProjectIdOrderByOrderIndexAsc(projectId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public DeliverableDTO getDeliverable(UUID id) {
        Deliverable deliverable = deliverableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deliverable not found"));
        return mapToDTO(deliverable);
    }

    @Transactional
    public DeliverableDTO createDeliverable(DeliverableDTO dto) {
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Deliverable parent = null;
        if (dto.getParentId() != null) {
            parent = deliverableRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent deliverable not found"));
        }

        User creator = null;
        // In a real app, get ID from SecurityContext
        if (dto.getCreatedById() != null) {
            creator = userRepository.findById(dto.getCreatedById()).orElse(null);
        }

        Deliverable deliverable = Deliverable.builder()
                .project(project)
                .parent(parent)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .type(dto.getType())
                .status(dto.getStatus() != null ? dto.getStatus() : "draft")
                .version(dto.getVersion() != null ? dto.getVersion() : "1.0")
                .orderIndex(dto.getOrderIndex() != null ? dto.getOrderIndex() : 0)
                .createdBy(creator)
                .build();

        return mapToDTO(deliverableRepository.save(deliverable));
    }

    @Transactional
    public DeliverableDTO updateDeliverable(UUID id, DeliverableDTO dto) {
        Deliverable deliverable = deliverableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deliverable not found"));

        deliverable.setTitle(dto.getTitle());
        deliverable.setDescription(dto.getDescription());
        deliverable.setType(dto.getType());
        deliverable.setStatus(dto.getStatus());
        deliverable.setVersion(dto.getVersion());
        deliverable.setOrderIndex(dto.getOrderIndex());

        return mapToDTO(deliverableRepository.save(deliverable));
    }

    public void deleteDeliverable(UUID id) {
        deliverableRepository.deleteById(id);
    }

    private DeliverableDTO mapToDTO(Deliverable d) {
        return DeliverableDTO.builder()
                .id(d.getId())
                .projectId(d.getProject().getId())
                .parentId(d.getParent() != null ? d.getParent().getId() : null)
                .title(d.getTitle())
                .description(d.getDescription())
                .type(d.getType())
                .status(d.getStatus())
                .version(d.getVersion())
                .orderIndex(d.getOrderIndex())
                .createdById(d.getCreatedBy() != null ? d.getCreatedBy().getId() : null)
                .createdByName(d.getCreatedBy() != null ? d.getCreatedBy().getUsername() : null)
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}