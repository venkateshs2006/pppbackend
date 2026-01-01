package com.java.ppp.pppbackend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientInfoDTO {
    private String name;           // Organization Contact Person
    private String nameEn;
    private String organization;   // Organization Name
    private String organizationEn;
    private String avatar;
    private String email;
}
