package com.java.ppp.pppbackend.service;

import com.java.ppp.pppbackend.dto.*;
import com.java.ppp.pppbackend.entity.*;
import com.java.ppp.pppbackend.exception.ResourceNotFoundException;
import com.java.ppp.pppbackend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final DeliverableRepository deliverableRepository;
    private final ClientRepository clientRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TicketRepository ticketRepository;
    //private final TaskRepository taskRepository; // Assuming you have tasks
    public List<Role> getAllProjectRole(){
        return roleRepository.findAll();
    }
    public List<ProjectResponseDTO> getProjectsForUser(User user) {
        List<Project> projects;

        // --- 1. Determine Scope based on Role ---
        boolean isSuperAdmin = user.getRoles().stream().anyMatch(r -> r.getName().getDbValue().equalsIgnoreCase("super_admin"));
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().getDbValue().equalsIgnoreCase("admin"));
        if (isSuperAdmin) {
            // Super Admin: See ALL projects in the system
            projects = projectRepository.findAll();
        } else {
            // Everyone else (Admin, Consultant, Client):
            // See only projects they are assigned to (or manage)
            // Since User is no longer tied to an Org, we can't fetch "All Org Projects" automatically
            projects = projectRepository.findProjectsByUserId(user.getId());
        }

        // --- 2. Map to DTOs ---
        return projects.stream()
                .map(this::mapToProjectResponseDTO)
                .collect(Collectors.toList());
    }


    @Transactional
    public ProjectResponseDTO getProject(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return mapToProjectResponseDTO(project);
    }

    @Transactional
    public List<TeamMemberSummaryDTO> getProjectMembers(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return mapTeamMembers(project.getMembers());
    }

    @Transactional
    public TeamMemberSummaryDTO addProjectMember(UUID projectId, Long userId, String role) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if user is already in the project
        boolean alreadyExists = project.getMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(userId));

        if (alreadyExists) {
            projectMemberRepository.updateMemberRole(projectId,userId,role);
            return mapTeamMember(projectMemberRepository.findProjectMemberDetails(projectId,userId,role));
        }
        else {
            // Create new association
            ProjectMember newMember = new ProjectMember();
            newMember.setProject(project);
            newMember.setUser(user);
            newMember.setRole(role.toUpperCase()); // Store as uppercase enum style usually
            newMember.setJoinedAt(LocalDateTime.now());
            projectMemberRepository.save(newMember);
            return mapTeamMember(newMember);
        }
    }

    @Transactional
    public void removeProjectMember(UUID projectId, Long userId, String role)  {
        // 1. Validate Project Exists
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        // 2. Find the specific member in this project
        // We look through the project's member list to find the one matching the userId
        ProjectMember memberToRemove = project.getMembers().stream()
                .filter(m -> m.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("User not found in this project"));

        // 3. (Optional) Safety Check: Verify the role matches before deleting
        // This ensures the frontend is trying to delete the correct role assignment
        if (!memberToRemove.getRole().equalsIgnoreCase(role)) {
            throw new IllegalArgumentException("Role mismatch: Member has role " + memberToRemove.getRole()
                    + " but requested to delete " + role);
        }

       // Important: Remove from the parent list AND delete explicitly to ensure JPA handles it correctly
        project.getMembers().remove(memberToRemove);
        projectMemberRepository.delete(memberToRemove);
    }
    @Transactional
    public ProjectResponseDTO createProject(ProjectDTO dto) {
        Project project = new Project();
        project.setProgress(0); // Default for new projects
        return saveOrUpdate(project, dto);
    }

    @Transactional
    public ProjectResponseDTO updateProject(UUID id, ProjectDTO dto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return saveOrUpdate(project, dto);
    }

    @Transactional
    public void deleteProject(UUID id) {
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Project not found");
        }
        projectRepository.deleteById(id);
    }

    private ProjectResponseDTO saveOrUpdate(Project project, ProjectDTO dto) {
        // Map Basic Fields
        project.setTitleAr(dto.getTitle());
        project.setTitleEn(dto.getTitleEn());
        project.setDescriptionAr(dto.getDescription());
        project.setDescriptionEn(dto.getDescriptionEn());
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
        project.setBudget(dto.getBudget());
        project.setSpent(dto.getSpent() != null ? dto.getSpent() : BigDecimal.ZERO);

        // Map Status (Force Uppercase)
        if (dto.getStatus() != null) {
            try {
                project.setStatus(dto.getStatus());
            } catch (Exception e) {
                project.setStatus(ProjectStatus.PLANNING);
            }
        } else if (project.getStatus() == null) {
            project.setStatus(ProjectStatus.PLANNING);
        }

        // Map Priority (Force Uppercase)
        if (dto.getPriority() != null) {
            try {
                project.setPriority(ProjectPriority.valueOf(dto.getPriority().toUpperCase()));
            } catch (Exception e) {
                project.setPriority(ProjectPriority.MEDIUM);
            }
        } else if (project.getPriority() == null) {
            project.setPriority(ProjectPriority.MEDIUM);
        }

        // Map Relationships
        if (dto.getClientId() != null) {
                Client org = clientRepository.findById(Long.valueOf(dto.getClientId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
            project.setClient(org);
        }

        if (dto.getProjectManagerId() != null) {
            User pm = userRepository.findById(dto.getProjectManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            project.setProjectManager(pm);
        }
        Project saved = projectRepository.save(project);
// 2. ✅ FIX: Create Deliverables from the List<String>
        if (dto.getDeliverables() != null && !dto.getDeliverables().isEmpty()) {
            List<Deliverable> deliverablesToSave = new ArrayList<>();

            for (String deliverableName : dto.getDeliverables()) {
                if (deliverableName != null && !deliverableName.trim().isEmpty()) {
                    Deliverable deliverable = new Deliverable();
                    deliverable.setTitle(deliverableName);

                    deliverable.setProject(saved); // Link to the new project

                    deliverable.setType(DeliverableType.GUIDE);

                    // OR if 'type' is an Enum in your Entity (Uncomment below):
                    // deliverable.setType(DeliverableType.DOCUMENT);

                    // ✅ FIX 2: Ensure Status is set (DRAFT or PENDING)
                    deliverable.setStatus(DeliverableStatus.DRAFT);
                    deliverablesToSave.add(deliverable);
                }
            }

            if (!deliverablesToSave.isEmpty()) {
                deliverableRepository.saveAll(deliverablesToSave);
            }
        }

        return mapToProjectResponseDTO(saved);
    }

    private List<TeamMemberSummaryDTO> mapTeamMembers(List<ProjectMember> members) {
        if (members == null) return Collections.emptyList();
        return members.stream()
                .map(m -> TeamMemberSummaryDTO.builder()
                        .userId(m.getUser().getId())
                        .name(m.getUser().getFirstName() + " " + m.getUser().getLastName())
                        .role(m.getRole()) // Project Role (e.g., 'Specialist')
                        .email(m.getUser().getEmail())
                        .avatar(m.getUser().getFirstName().substring(0, 1))
                        .phoneNumber(m.getUser().getPhoneNumber())
                        .jobTitle(m.getUser().getJobTitle())
                        .build())
                .collect(Collectors.toList());
    }

    private TeamMemberSummaryDTO mapTeamMember(ProjectMember member) {

        return TeamMemberSummaryDTO.builder()
                        .userId(member.getUser().getId())
                        .name(member.getUser().getFirstName() + " " + member.getUser().getLastName())
                        .role(member.getRole()) // Project Role (e.g., 'Specialist')
                        .email(member.getUser().getEmail())
                        .avatar(member.getUser().getFirstName().substring(0, 1))
                        .phoneNumber(member.getUser().getPhoneNumber())
                        .jobTitle(member.getUser().getJobTitle())
                        .build();

    }

    public List<DeliverableDto> getProjectDeliverables(UUID projectId) {
        // Use the repository method we added earlier
        List<Deliverable> deliverables = deliverableRepository.findByProjectId(projectId);

        return deliverables.stream()
                .map(d -> DeliverableDto.builder()
                        .id(d.getId())
                        .title(d.getTitle()) // or getTitle()
                        .status(d.getStatus()) // Ensure this matches your Enum/String
                        .createdAt(d.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
    private ProjectResponseDTO mapToProjectResponseDTO(Project p) {
        List<Deliverable> deliverableList=p.getDeliverables();
        long deliverables=deliverableList.size();
        long completedDeliverables=deliverableList.stream().filter(d->d.getStatus()==DeliverableStatus.COMPLETED).count();

        return ProjectResponseDTO.builder()
                .id(p.getId().toString())
                .title(p.getTitleAr())
                .titleEn(p.getTitleEn())
                .description(p.getDescriptionAr())
                .descriptionEn(p.getDescriptionEn())
                .status(p.getStatus().name().toLowerCase()) // Lowercase for Frontend UI
                .priority(p.getPriority().name().toLowerCase())
                .progress(p.getProgress() != null ? p.getProgress() : 0)
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .budget(p.getBudget())
                .spent(p.getSpent())
                .client(mapClient(p.getClient()))
                .consultant(mapConsultant(p.getProjectManager()))
                .team(mapTeamMembers(p.getMembers()))
                .deliverables(deliverables)
                .completedDeliverables(completedDeliverables)
                .build();
    }

    private ClientInfoDTO mapClient(Client org) {
        if (org == null) return null;
        return ClientInfoDTO.builder()
                .id(org.getId())
                .organization(org.getName())
                .organizationEn(org.getName()) // Use name as fallback or add nameEn to Org entity
                .name(org.getName()) // Ideally fetch contact person
                .email(org.getStripeCustomerId())
                .avatar(org.getName().substring(0, 2).toUpperCase())
                .build();
    }

    private ConsultantInfoDTO mapConsultant(User user) {
        if (user == null) return null;
        return ConsultantInfoDTO.builder()
                .userId(user.getId())
                .name(user.getFirstName() + " " + user.getLastName())
                .jobTitle(user.getJobTitle() != null ? user.getJobTitle() : "Project Manager")
                .avatar(user.getFirstName().substring(0, 1).toUpperCase())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getJobTitle())
                .build();
    }

    private ClientInfoDTO mapClientInfo(Project p) {
        if (p.getClient() == null) return null;
        return ClientInfoDTO.builder()
                .id(p.getClient().getId())
                .organization(p.getClient().getName())
                .organizationEn(p.getClient().getName()) // Placeholder
                .name("Admin Contact") // Ideally fetch from Org Contact Person
                .email("admin@org.com")
                .avatar("OR")
                .build();
    }

    private ConsultantInfoDTO mapConsultantInfo(User pm) {
        if (pm == null) return null;
        return ConsultantInfoDTO.builder()
                .name(pm.getFirstName() + " " + pm.getLastName())
                .role("Lead Consultant")
                .avatar(pm.getFirstName().substring(0, 1))
                .phoneNumber(pm.getPhoneNumber())
                .build();
    }

}