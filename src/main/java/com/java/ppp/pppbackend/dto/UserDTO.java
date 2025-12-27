package com.java.ppp.pppbackend.dto;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder

public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String department;
    private String jobTitle;
    private String avatarUrl;
    private Boolean isEmailVerified;
    private String bio;
    private String preferences;
    private Boolean isActive;
    private Boolean isVerified;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private LocalDateTime lastLoginAt;
    private List<String> roles;
}