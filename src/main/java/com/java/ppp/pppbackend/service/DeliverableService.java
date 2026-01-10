package com.java.ppp.pppbackend.service;
import com.java.ppp.pppbackend.dto.DeliverableDTO;
import com.java.ppp.pppbackend.entity.*;
import com.java.ppp.pppbackend.exception.ResourceNotFoundException;
import com.java.ppp.pppbackend.repository.*;
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

    @Transactional(readOnly = true)
    public List<DeliverableDTO> getProjectDeliverables(UUID projectId) {
        return deliverableRepository.findByProjectIdOrderByOrderIndexAsc(projectId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DeliverableDTO createDeliverable(DeliverableDTO dto) {
        Deliverable deliverable = new Deliverable();
        return saveOrUpdate(deliverable, dto);
    }

    @Transactional
    public DeliverableDTO updateDeliverable(UUID id, DeliverableDTO dto) {
        Deliverable deliverable = deliverableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deliverable not found"));
        return saveOrUpdate(deliverable, dto);
    }

    @Transactional
    public void deleteDeliverable(UUID id) {
        if (!deliverableRepository.existsById(id)) {
            throw new ResourceNotFoundException("Deliverable not found");
        }
        deliverableRepository.deleteById(id);
    }

    private DeliverableDTO saveOrUpdate(Deliverable entity, DeliverableDTO dto) {
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : "DRAFT");
        entity.setVersion(dto.getVersion() != null ? dto.getVersion() : "1.0");
        entity.setOrderIndex(dto.getOrderIndex() != null ? dto.getOrderIndex() : 0);

        // Map Type (Enum)
        if (dto.getType() != null) {
            try {
                entity.setType(DeliverableType.valueOf(dto.getType().toUpperCase()));
            } catch (Exception e) {
                // Handle invalid type or default
                entity.setType(DeliverableType.TOPIC);
            }
        }

        // Map Project
        if (dto.getProjectId() != null) {
            Project project = projectRepository.findById(dto.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
            entity.setProject(project);
        }

        // Map Creator
        if (dto.getCreatedById() != null) {
            User user = userRepository.findById(dto.getCreatedById())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            entity.setCreatedBy(user);
        }

        return mapToDTO(deliverableRepository.save(entity));
    }

    private DeliverableDTO mapToDTO(Deliverable d) {
        return DeliverableDTO.builder()
                .id(d.getId())
                .title(d.getTitle())
                .description(d.getDescription())
                .type(d.getType() != null ? d.getType().name().toLowerCase() : null)
                .status(d.getStatus())
                .version(d.getVersion())
                .orderIndex(d.getOrderIndex())
                .projectId(d.getProject() != null ? d.getProject().getId() : null)
                .createdById(d.getCreatedBy() != null ? d.getCreatedBy().getId() : null)
                .createdByName(d.getCreatedBy() != null ? d.getCreatedBy().getUsername() : null) // or getFirstName
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }


    public List<DeliverableDTO> getProjectDeliverable() {
        return deliverableRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

}