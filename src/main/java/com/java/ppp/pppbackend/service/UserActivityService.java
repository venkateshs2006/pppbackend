package com.java.ppp.pppbackend.service;

import com.java.ppp.pppbackend.entity.User;
import com.java.ppp.pppbackend.entity.UserActivity;
import com.java.ppp.pppbackend.repository.UserActivityRepository;
import com.java.ppp.pppbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserActivityService {

    private final UserActivityRepository userActivityRepository;
    private final UserRepository userRepository;

    @Transactional
    public void logActivity(Long userId, String action, String description,
                            String entityType, Long entityId, String ipAddress) {
        try {
            User user = userRepository.findById(userId).orElse(null);

            UserActivity activity = UserActivity.builder()
                    .user(user)
                    .activityType(action)
                    .description(description)
                    .entityType(entityType)
                    .entityId(entityId)
                    .ipAddress(ipAddress)
                    .build();

            userActivityRepository.save(activity);
        } catch (Exception e) {
            log.error("Error logging user activity", e);
        }
    }

    public List<UserActivity> getUserActivities(Long userId) {
        return userActivityRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
