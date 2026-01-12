package com.java.ppp.pppbackend.service;

import com.fasterxml.classmate.MemberResolver;
import com.java.ppp.pppbackend.dto.DeliverableDto;
import com.java.ppp.pppbackend.entity.Deliverable;
import com.java.ppp.pppbackend.entity.DeliverableStatus;
import com.java.ppp.pppbackend.entity.Project;
import com.java.ppp.pppbackend.repository.DeliverableRepository;
import com.java.ppp.pppbackend.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliverableService {

    private final DeliverableRepository repository;
    private final ProjectRepository projectRepository;
    @Value("${file.upload-dir}")
    String uploadDir;

    // Inject ProjectRepository to update Project status if needed

    public DeliverableDto createDeliverable(DeliverableDto dto) {
        Deliverable entity = mapToEntity(dto);
        // Default logic
        entity.setStatus(DeliverableStatus.DRAFT);
        return mapToDto(repository.save(entity));
    }

    public DeliverableDto updateDeliverable(UUID id, DeliverableDto dto) {
        Deliverable existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deliverable not found"));

        existing.setTitle(dto.getTitle());
        existing.setTitleEn(dto.getTitleEn());
        existing.setDescription(dto.getDescription());
        existing.setStatus(dto.getStatus());

        // Handle file update logic here if fileUrl is present
        if(dto.getFileUrl() != null) {
            existing.setFileUrl(dto.getFileUrl());
            existing.setFileName(dto.getFileName());
        }

        return mapToDto(repository.save(existing));
    }

    // Assign to Client for Approval
    public void submitForApproval(UUID id, Long clientId) {
        Deliverable d = repository.findById(id).orElseThrow();
        d.setStatus(DeliverableStatus.REVIEW);
        d.setAssignedToId(clientId);
        repository.save(d);
    }

    // Approve or Reject
    public void reviewDeliverable(UUID id, boolean approved, String comments) {
        Deliverable d = repository.findById(id).orElseThrow();
        if(approved) {
            d.setStatus(DeliverableStatus.APPROVED);
        } else {
            d.setStatus(DeliverableStatus.REJECTED);
            // Ideally store comments in a separate Audit/Comment table
        }
        repository.save(d);
    }

    public List<DeliverableDto> getByProject(UUID projectId) {
        return repository.findByProjectId(projectId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public void deleteDeliverable(UUID id) {
        repository.deleteById(id);
    }

    public DeliverableDto uploadFile(UUID deliverableId, MultipartFile file) throws IOException {
        // 1. Validate the Deliverable exists
        Deliverable deliverable = repository.findById(deliverableId)
                .orElseThrow(() -> new RuntimeException("Deliverable not found with id " + deliverableId));
        try {
            Path fileStorageLocation=Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create upload directory", ex);
        }
        // 2. Normalize file name
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        // OPTIONAL: Generate a unique filename to prevent overwrites
        // String fileName = UUID.randomUUID().toString() + "_" + originalFileName;
        // For now, keeping original:
        Path fileStorageLocation=Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create upload directory", ex);
        }
        String fileName = originalFileName;

        try {
            // 3. Check for invalid characters
            if(fileName.contains("..")) {
                throw new RuntimeException("Filename contains invalid path sequence " + fileName);
            }

            // 4. Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 5. Update Entity with file details
            deliverable.setFileUrl(targetLocation.toString()); // Full path for internal retrieval
            deliverable.setFileName(fileName);                  // Name for display
            deliverable.setFileType(file.getContentType());     // MIME type

            // 6. Save to DB
            Deliverable updatedDeliverable = repository.save(deliverable);

            // 7. Return DTO (Map entity to DTO)
            return mapToDto(updatedDeliverable);

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }
    public DeliverableDto getById(UUID id) {
        return repository.findById(id).map(this::mapToDto).orElseThrow();
    }
    public Resource loadFileAsResource(String fileName) {
        try {
            Path fileStorageLocation=Paths.get(uploadDir).toAbsolutePath().normalize();
            try {
                Files.createDirectories(fileStorageLocation);
            } catch (Exception ex) {
                throw new RuntimeException("Could not create upload directory", ex);
            }
            // 1. Resolve the full path (e.g., ./uploads/deliverables/myfile.pdf)
            Path filePath = fileStorageLocation.resolve(fileName).normalize();

            // 2. Convert path to a URL Resource (allows reading the file)
            Resource resource = new UrlResource(filePath.toUri());

            // 3. Check if file actually exists and is readable
            if(resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found or not readable: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File path invalid: " + fileName, ex);
        }
    }
    private DeliverableDto mapToDto(Deliverable entity) {
        DeliverableDto dto = new DeliverableDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setTitleEn(entity.getTitleEn());
        dto.setDescription(entity.getDescription());
        dto.setType(entity.getType());
        dto.setStatus(entity.getStatus());
        if (entity.getProject() != null) {
            dto.setProjectId(entity.getProject().getId());
        }
        //dto.setFileUrl(entity.getFileUrl());
        dto.setFileUrl("/deliverables/download/" + entity.getFileName());
        dto.setFileName(entity.getFileName());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }


    // Updated Mapping Method (DTO -> Entity)
    private Deliverable mapToEntity(DeliverableDto dto) {
        // 1. Fetch the project reference
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found with ID: " + dto.getProjectId()));

        // 2. Build entity with the relation
        return Deliverable.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .titleEn(dto.getTitleEn())
                .description(dto.getDescription())
                .type(dto.getType())
                .status(dto.getStatus())
                .project(project) // Set the object, not the ID
                .parentId(dto.getParentId())
                .build();
    }
}
