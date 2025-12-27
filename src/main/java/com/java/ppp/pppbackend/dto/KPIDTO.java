package com.java.ppp.pppbackend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KPIDTO {
    private String label;
    private String labelEn;
    private int percentage;
    private String colorClass; // e.g., "bg-green-600"
}
