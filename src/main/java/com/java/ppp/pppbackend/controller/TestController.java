package com.java.ppp.pppbackend.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Test", description = "Test API")
public class TestController {

    @GetMapping("/hello")
    @Operation(summary = "Hello endpoint")
    @PreAuthorize("hasRole('ADMIN','USER','EDITOR')")
    public String hello() {
        return "Hello from PPP!";
    }

    @GetMapping("/ping")
    @Operation(summary = "Ping endpoint")
    @PreAuthorize("hasRole('ADMIN','USER','EDITOR')")
    public String ping() {
        return "pong";
    }
}