package com.java.ppp.pppbackend.dto;


import lombok.Data;

import java.util.List;

@Data
public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private Long userId;
    private String username;
    private String email;
    private List<String> roles;
}