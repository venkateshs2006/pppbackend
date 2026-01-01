package com.java.ppp.pppbackend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConsultantInfoDTO {
    private String name;
    private String role;
    private String avatar;
}
