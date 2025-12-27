package com.java.ppp.pppbackend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecentDeliverableDTO {
    private String id;
    private String title;
    private String titleEn;
    private String type;       // 'policy', 'guide', etc.
    private String status;     // 'approved', 'review'
    private String projectName;
    private String projectNameEn;
    private String version;
}
