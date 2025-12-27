package com.java.ppp.pppbackend.service;

import com.java.ppp.pppbackend.dto.ProjectFileDTO;
import com.java.ppp.pppbackend.entity.ProjectFile;
import com.java.ppp.pppbackend.entity.User;
import com.java.ppp.pppbackend.repository.ProjectFileRepository;
import com.java.ppp.pppbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectFileService {

    private final ProjectFileRepository fileRepository;
    private final UserRepository userRepository;

    public List<ProjectFileDTO> getFilesByProject(UUID projectId) {
        return fileRepository.findByProjectIdAndIsActiveTrue(projectId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ProjectFileDTO addFile(ProjectFileDTO dto) {
        User user = null;
        if(dto.getUploadedById() != null) {
            user = userRepository.findById(dto.getUploadedById()).orElse(null);
        }

        ProjectFile file = ProjectFile.builder()
                .id(dto.getId())
                .name(dto.getName())
                .fileType(dto.getFileType())
                .fileUrl(dto.getFileUrl())
                .fileSize(dto.getFileSize())
                .mimeType(dto.getMimeType())
                .uploadedBy(user)
                .version(1)
                .isActive(true)
                .build();

        return mapToDTO(fileRepository.save(file));
    }

    public void deleteFile(Long id) {
        // Soft delete
        fileRepository.findById(id).ifPresent(file -> {
            file.setIsActive(false);
            fileRepository.save(file);
        });
    }

    private ProjectFileDTO mapToDTO(ProjectFile file) {
        return ProjectFileDTO.builder()
                .id(file.getId())
                .name(file.getName())
                .fileType(file.getFileType())
                .fileUrl(file.getFileUrl())
                .fileSize(file.getFileSize())
                .mimeType(file.getMimeType())
                .uploadedById(file.getUploadedBy() != null ? file.getUploadedBy().getId() : null)
                .uploadedByName(file.getUploadedBy() != null ? file.getUploadedBy().getUsername() : null)
                .version(file.getVersion())
                .isActive(file.getIsActive())
                .createdAt(file.getCreatedAt())
                .build();
    }
}