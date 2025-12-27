package com.java.ppp.pppbackend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class RecentProjectDTO {
    private String id;
    private String title;      // Arabic title
    private String titleEn;    // English title
    private int progress;
    private String status;     // e.g., 'active', 'review'
    private String clientName;
    private String clientNameEn;
    private LocalDate dueDate;
}
