package com.java.ppp.pppbackend.dto;

import com.java.ppp.pppbackend.entity.RoleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class UserSelectionDTO {
    private Long id;
    private String name;
    private String email;
    private String jobTitle;
    private RoleType role;
}