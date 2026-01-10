package com.java.ppp.pppbackend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamMemberSummaryDTO {
    private String name;
    private String role;
    private String avatar;
    private String email;
    private String phoneNumber;
    private String jobTitle;
    private Long userId;
}
