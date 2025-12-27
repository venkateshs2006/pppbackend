package com.java.ppp.pppbackend.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateContentRequest {
    private String title;
    private String description;
    private String body;
    private Long categoryId;
    private Set<String> tags;
    private String changeSummary;
}