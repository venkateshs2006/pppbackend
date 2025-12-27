package com.java.ppp.pppbackend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SecurityConfigCheck implements CommandLineRunner {

    @Autowired
    private ApplicationContext context;

    @Autowired(required = false)
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("=====================================");
        log.info("SECURITY CONFIGURATION CHECK");
        log.info("=====================================");

        // Check which UserDetailsService is being used
        UserDetailsService userDetailsService = null;
        if (userDetailsService != null) {
            log.info("✓ UserDetailsService found: {}", userDetailsService.getClass().getSimpleName());

            if (userDetailsService.getClass().getSimpleName().contains("InMemory")) {
                log.error("✗ PROBLEM: Using InMemoryUserDetailsManager!");
                log.error("  This means your CustomUserDetailsService is not being used!");
            } else if (userDetailsService.getClass().getSimpleName().contains("Custom")) {
                log.info("✓ CORRECT: Using CustomUserDetailsService!");
            }
        } else {
            log.error("✗ No UserDetailsService found!");
        }

        // Check PasswordEncoder
        log.info("✓ PasswordEncoder: {}", passwordEncoder.getClass().getSimpleName());

        // Test password encoding
        String testPassword = "password123";
        String encoded = passwordEncoder.encode(testPassword);
        log.info("✓ Test password encoding works");
        log.info("  Plain: {}", testPassword);
        log.info("  Encoded: {}...", encoded.substring(0, 30));

        // Try to load a test user
        if (userDetailsService != null && !userDetailsService.getClass().getSimpleName().contains("InMemory")) {
            try {
                log.info("Attempting to load test user 'admin'...");
                var userDetails = userDetailsService.loadUserByUsername("admin");
                log.info("✓ Successfully loaded user: {}", userDetails.getUsername());
                log.info("  Authorities: {}", userDetails.getAuthorities());
            } catch (Exception e) {
                log.error("✗ Failed to load user 'admin': {}", e.getMessage());
            }
        }

        log.info("=====================================");
    }
}