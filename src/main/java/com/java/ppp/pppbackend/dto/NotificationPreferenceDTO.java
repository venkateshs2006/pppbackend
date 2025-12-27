package com.java.ppp.pppbackend.dto;

import com.java.ppp.pppbackend.entity.DigestFrequency;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceDTO {
    private Long id;
    private Long userId;

    @NotNull
    private Boolean emailEnabled;

    @NotNull
    private Boolean inAppEnabled;

    @NotNull
    private Boolean pushEnabled;

    @NotNull
    private Boolean contentUpdates;

    @NotNull
    private Boolean commentsEnabled;

    @NotNull
    private Boolean mentionsEnabled;

    @NotNull
    private Boolean workflowNotifications;

    @NotNull
    private Boolean systemAlerts;

    @NotNull
    private DigestFrequency digestFrequency;
}

