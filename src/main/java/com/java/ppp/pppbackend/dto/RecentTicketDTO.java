package com.java.ppp.pppbackend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class RecentTicketDTO {
    private String id;
    private String title;
    private String titleEn;
    private String priority;
    private String status;
    private String projectName;
    private String projectNameEn;
    private LocalDate createdAt;
}
