package com.java.ppp.pppbackend.repository;

import com.java.ppp.pppbackend.entity.ProjectFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectFileRepository extends JpaRepository<ProjectFile, Long> {
    List<ProjectFile> findByProjectIdAndIsActiveTrue(UUID projectId);
}