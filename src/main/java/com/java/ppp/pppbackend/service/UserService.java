package com.java.ppp.pppbackend.service;


import com.java.ppp.pppbackend.dto.UserDTO;
import com.java.ppp.pppbackend.entity.Role;
import com.java.ppp.pppbackend.entity.User;
import com.java.ppp.pppbackend.exception.BadRequestException;
import com.java.ppp.pppbackend.exception.ResourceNotFoundException;
import com.java.ppp.pppbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        log.debug("Creating user: {}", userDTO.getUsername());

        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new BadRequestException("Username already exists");
        }

        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        User user = new User();

        // 1. Map basic fields
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setJobTitle(userDTO.getJobTitle());


        // 2. CRITICAL FIX: Set and Encode the Password
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        } else {
            // Optional: Set a default temporary password if none provided
            // user.setPassword(passwordEncoder.encode("Temp@123"));
            throw new IllegalArgumentException("Password cannot be empty");
        }

        // 3. Set defaults
        user.setIsActive(true);
        user.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        user.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

        User saved = userRepository.save(user);
        return mapToDTO(saved);
    }


    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        log.debug("Updating user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (userDTO.getFirstName() != null) {
            user.setFirstName(userDTO.getFirstName());
        }
        if (userDTO.getLastName() != null) {
            user.setLastName(userDTO.getLastName());
        }
        if (userDTO.getPhoneNumber() != null) {
            user.setPhoneNumber(userDTO.getPhoneNumber());
        }
        if (userDTO.getBio() != null) {
            user.setBio(userDTO.getBio());
        }
        if (userDTO.getDepartment() != null) {
            user.setDepartment(userDTO.getDepartment());
        }
        if (userDTO.getJobTitle() != null) {
            user.setJobTitle(userDTO.getJobTitle());
        }

        user.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        User updated = userRepository.save(user);

        return mapToDTO(updated);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        log.debug("Getting user by id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return mapToDTO(user);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        log.debug("Getting user by username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return mapToDTO(user);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        log.debug("Getting user by email: {}", email);

        User user =userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToDTO(user);
    }


    @Transactional
    public void deleteUser(Long id) {
        log.debug("Deleting user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        log.debug("Getting all users with pagination");

        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllActiveUsers() {
        log.debug("Getting all active users");

        List<User> users = userRepository.findAllActiveUsers();
        return users.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getActiveUsers(Pageable pageable) {
        log.debug("Getting active users with pagination");

        Page<User> users = userRepository.findByIsActive(true, pageable);
        return users.map(this::mapToDTO);
    }


    @Transactional(readOnly = true)
    public Page<UserDTO> searchUsers(String searchTerm, Pageable pageable) {
        log.debug("Searching users with term: {}", searchTerm);

        Page<User> users = userRepository.searchUsers(searchTerm, pageable);
        return users.map(this::mapToDTO);
    }


    @Transactional
    public void activateUser(Long id) {
        log.debug("Activating user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setIsActive(true);
        user.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        userRepository.save(user);
    }


    @Transactional
    public void deactivateUser(Long id) {
        log.debug("Deactivating user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setIsActive(false);
        user.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        userRepository.save(user);
    }


    @Transactional(readOnly = true)
    public long getTotalUsers() {
        return userRepository.count();
    }


    @Transactional(readOnly = true)
    public long getActiveUsersCount() {
        return userRepository.countActiveUsers();
    }

    // Helper method to map entity to DTO
    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFirstName()+user.getLastName())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .bio(user.getBio())
                .avatarUrl(user.getProfileImageUrl()==null?user.getFirstName().substring(0,1):user.getProfileImageUrl())
                .department(user.getDepartment())
                .jobTitle(user.getJobTitle())
                .isActive(user.getIsActive())
                .roles(user.getRoles().stream().map(r->r.getName().getDbValue()).collect(Collectors.toList()))
                .isEmailVerified(user.getIsEmailVerified())
                .lastLoginAt(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }


}