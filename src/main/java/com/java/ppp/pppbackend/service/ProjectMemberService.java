package com.java.ppp.pppbackend.service;

import com.java.ppp.pppbackend.dto.ProjectMemberDTO;
import com.java.ppp.pppbackend.entity.ProjectMember;
import com.java.ppp.pppbackend.entity.User;
import com.java.ppp.pppbackend.repository.ProjectMemberRepository;
import com.java.ppp.pppbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {

    private final ProjectMemberRepository memberRepository;
    private final UserRepository userRepository;

    public List<ProjectMemberDTO> getMembers(UUID projectId) {
        return memberRepository.findByProjectId(projectId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ProjectMemberDTO addMember(ProjectMemberDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (memberRepository.findByProjectIdAndUserId(dto.getProjectId(), dto.getUserId()).isPresent()) {
            throw new RuntimeException("User is already a member of this project");
        }

        ProjectMember member = ProjectMember.builder()
                .id(dto.getId())
                .user(user)
                .role(dto.getRole())
                .permissions(dto.getPermissions())
                .build();

        return mapToDTO(memberRepository.save(member));
    }

    public void removeMember(UUID memberId) {
        memberRepository.deleteById(memberId);
    }

    private ProjectMemberDTO mapToDTO(ProjectMember member) {
        return ProjectMemberDTO.builder()
                .id(member.getId())
                .projectId(member.getProject().getId())
                .userId(member.getUser().getId())
                .userName(member.getUser().getUsername())
                .userEmail(member.getUser().getEmail())
                .role(member.getRole())
                .permissions(member.getPermissions())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}