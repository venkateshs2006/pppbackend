package com.java.ppp.pppbackend.service;

import com.java.ppp.pppbackend.dto.ProjectDTO;
import com.java.ppp.pppbackend.dto.ProjectFileDTO;
import com.java.ppp.pppbackend.dto.ProjectMemberDTO;
import com.java.ppp.pppbackend.entity.*;
import com.java.ppp.pppbackend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectFileRepository fileRepository;
    private final ProjectMemberRepository memberRepository;
    private final UserRepository userRepository; // Assuming this exists
    private final OrganizationRepository organizationRepository;
    // --- Project Operations ---

    public ProjectDTO createProject(ProjectDTO dto) {
        User manager = null;
        if (dto.getProjectManagerId() != null) {
            manager = userRepository.findById(dto.getProjectManagerId())
                    .orElseThrow(() -> new RuntimeException("Project Manager not found"));
        }

        Project project = Project.builder()
                .organization(organizationRepository.findById(dto.getOrganizationId()).orElseThrow(() -> {
                    log.error("Organization not found in database: {}", dto.getOrganizationId());
                    return new UsernameNotFoundException("User not found: " + dto.getOrganizationId());
                })).name(dto.getName())
                .description(dto.getDescription())
                .status(dto.getStatus() != null ? dto.getStatus() : ProjectStatus.PLANNING)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .budget(dto.getBudget())
                .progress(0)
                .projectManager(manager)
                .build();

        return mapToProjectDTO(projectRepository.save(project));
    }

    public ProjectDTO getProject(UUID id) {
        return projectRepository.findById(id)
                .map(this::mapToProjectDTO)
                .orElseThrow(() -> new RuntimeException("Project not found"));
    }

    public List<ProjectDTO> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::mapToProjectDTO)
                .collect(Collectors.toList());
    }

    // --- File Operations ---

    public List<ProjectFileDTO> getProjectFiles(UUID projectId) {
        return fileRepository.findByProjectIdAndIsActiveTrue(projectId).stream()
                .map(this::mapToFileDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectFileDTO addFileToProject(UUID projectId, ProjectFileDTO fileDto) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User uploader = userRepository.findById(fileDto.getUploadedById())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProjectFile file = ProjectFile.builder()
                .project(project)
                .name(fileDto.getName())
                .fileType(fileDto.getFileType())
                .fileUrl(fileDto.getFileUrl())
                .fileSize(fileDto.getFileSize())
                .mimeType(fileDto.getMimeType())
                .uploadedBy(uploader)
                .version(1)
                .isActive(true)
                .build();

        return mapToFileDTO(fileRepository.save(file));
    }

    // --- Member Operations ---

    public List<ProjectMemberDTO> getProjectMembers(UUID projectId) {
        return memberRepository.findByProjectId(projectId).stream()
                .map(this::mapToMemberDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectMemberDTO addMember(UUID projectId, ProjectMemberDTO memberDto) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User user = userRepository.findById(memberDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (memberRepository.findByProjectIdAndUserId(projectId, memberDto.getUserId()).isPresent()) {
            throw new RuntimeException("User is already a member of this project");
        }

        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(user)
                .role(memberDto.getRole())
                .permissions(memberDto.getPermissions())
                .build();

        return mapToMemberDTO(memberRepository.save(member));
    }

    // --- Mappers ---

    private ProjectDTO mapToProjectDTO(Project p) {
        return ProjectDTO.builder()
                .id(p.getId())
                .organizationId(p.getOrganization().getId())
                .name(p.getName())
                .description(p.getDescription())
                .status(p.getStatus())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .budget(p.getBudget())
                .progress(p.getProgress())
                .projectManagerId(p.getProjectManager() != null ? p.getProjectManager().getId() : null)
                .projectManagerName(p.getProjectManager() != null ?
                        p.getProjectManager().getFirstName() + " " + p.getProjectManager().getLastName() : null)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private ProjectFileDTO mapToFileDTO(ProjectFile f) {
        return ProjectFileDTO.builder()
                .id(f.getId())
                .name(f.getName())
                .fileType(f.getFileType())
                .fileUrl(f.getFileUrl())
                .fileSize(f.getFileSize())
                .mimeType(f.getMimeType())
                .uploadedById(f.getUploadedBy() != null ? f.getUploadedBy().getId() : null)
                .uploadedByName(f.getUploadedBy() != null ? f.getUploadedBy().getUsername() : null)
                .version(f.getVersion())
                .isActive(f.getIsActive())
                .createdAt(f.getCreatedAt())
                .build();
    }

    private ProjectMemberDTO mapToMemberDTO(ProjectMember m) {
        return ProjectMemberDTO.builder()
                .id(m.getId())
                .projectId(m.getProject().getId())
                .userId(m.getUser().getId())
                .userName(m.getUser().getFirstName() + " " + m.getUser().getLastName())
                .userEmail(m.getUser().getEmail())
                .role(m.getRole())
                .permissions(m.getPermissions())
                .joinedAt(m.getJoinedAt())
                .build();
    }
}