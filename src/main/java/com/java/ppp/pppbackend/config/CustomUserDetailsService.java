package com.java.ppp.pppbackend.config;

import com.java.ppp.pppbackend.entity.User;
import com.java.ppp.pppbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service("customUserDetailsService")  // ← Add name to be explicit
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("===================================");
        log.info("CustomUserDetailsService: Loading user '{}'", username);
        log.info("===================================");

        User user = userRepository.findByUsername(username).orElseThrow(() -> {
            log.error("User not found in database: {}", username);
            return new UsernameNotFoundException("User not found: " + username);
        });

        log.info("✓ User found in database");
        log.info("  - Username: {}", user.getUsername());
        log.info("  - Email: {}", user.getEmail());
        log.info("  - Active: {}", user.getIsActive());
        log.info("  - Password (first 20 chars): {}",
                user.getPassword() != null ? user.getPassword().substring(0, Math.min(20, user.getPassword().length())) : "NULL");
        log.info("  - Roles count: {}", user.getRoles() != null ? user.getRoles().size() : 0);

        if (user.getRoles() != null) {
            user.getRoles().forEach(role ->
                    log.info("    * Role: {}", role.getName())
            );
        }

        Collection<? extends GrantedAuthority> authorities = getAuthorities(user);
        log.info("  - Total authorities: {}", authorities.size());

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!user.getIsActive())
                .credentialsExpired(false)
                .disabled(!user.getIsActive())
                .build();

        log.info("✓ UserDetails created successfully");
        log.info("===================================");

        return userDetails;
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            user.getRoles().forEach(role -> {
                // Add role with ROLE_ prefix
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName().name()));

                // Add permissions if available
                if (role.getPermissions() != null) {
                    role.getPermissions().forEach(permission -> {
                        authorities.add(new SimpleGrantedAuthority(permission.getName()));
                    });
                }
            });
        } else {
            // Fallback: add default USER role if no roles assigned
            log.warn("User {} has no roles! Adding default USER role", user.getUsername());
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return authorities;
    }
}