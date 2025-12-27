package com.java.ppp.pppbackend.repository;

import com.java.ppp.pppbackend.entity.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
    List<UserActivity> findByUserId(Long userId);

    @Query("SELECT ua FROM UserActivity ua WHERE ua.user.id = :userId AND ua.createdAt BETWEEN :startDate AND :endDate")
    List<UserActivity> findUserActivitiesByDateRange(@Param("userId") Long userId,
                                                     @Param("startDate") OffsetDateTime startDate,
                                                     @Param("endDate") OffsetDateTime endDate);

    List<UserActivity> findByActivityType(String activityType);

    @Query("SELECT ua FROM UserActivity ua WHERE ua.user.id = :userId ORDER BY ua.createdAt DESC LIMIT :limit")
    List<UserActivity> findRecentUserActivities(@Param("userId") Long userId, @Param("limit") int limit);

    List<UserActivity> findByUserIdOrderByCreatedAtDesc(Long userId);
}

