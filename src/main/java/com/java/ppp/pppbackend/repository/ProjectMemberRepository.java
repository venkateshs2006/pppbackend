package com.java.ppp.pppbackend.repository;

import com.java.ppp.pppbackend.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {
    List<ProjectMember> findByProjectId(UUID projectId);

    Optional<ProjectMember> findByProjectIdAndUserId(UUID projectId, Long userId);
}
