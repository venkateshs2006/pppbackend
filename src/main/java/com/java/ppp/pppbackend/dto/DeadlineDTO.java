package com.java.ppp.pppbackend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeadlineDTO {
    private String title;
    private String titleEn;
    private String projectName;
    private String projectNameEn;
    private int daysRemaining;
}
