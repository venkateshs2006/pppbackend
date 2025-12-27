package com.java.ppp.pppbackend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ProjectFileDTO {
    private Long id;
    private String name;
    private String fileType;
    private String fileUrl;
    private Long fileSize;
    private String mimeType;
    private Long uploadedById;
    private String uploadedByName;
    private Integer version;
    private Boolean isActive;
    private LocalDateTime createdAt;
}