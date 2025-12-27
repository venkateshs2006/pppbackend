package com.java.ppp.pppbackend.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ProjectMemberDTO {
    private UUID id;
    private UUID projectId;
    private Long userId;
    private String userName;
    private String userEmail;
    private String role;
    private JsonNode permissions;
    private LocalDateTime joinedAt;
}