package com.java.ppp.pppbackend.dto;

import com.java.ppp.pppbackend.entity.TicketPriority;
import com.java.ppp.pppbackend.entity.TicketStatus;
import com.java.ppp.pppbackend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDTO {

    private UUID id;
    private String title;
    private String titleEn;
    private String description;
    private String descriptionEn;

    // Enum values as lowercase strings: 'open', 'in_progress', etc.
    private TicketStatus status;
    private TicketPriority priority;

    private String category;
    private String categoryEn;

    private ProjectInfo project;
    private ClientInfo client;
    private AssignedInfo assignedTo; // Optional (can be null)
    private Long createdById;
    private String createdByName;
    private String createdAt;
    private String updatedAt;
    private String dueDate;

    private List<TicketResponseDTO> responses;

    private List<String> attachments; // URLs
    private List<String> tags;
    private List<String> tagsEn;

    // --- Nested Classes to match UI Structure ---

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectInfo {
        private UUID id;
        private String name;
        private String nameEn;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientInfo {
        private String name;           // Main Client Name
        private String nameEn;
        private String organization;   // Organization Name
        private String organizationEn;
        private String avatar;         // Initials or URL
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignedInfo {
        private String id;
        private String name;
        private String role;
        private String avatar;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketResponseDTO {
        private String id;
        private ResponseAuthor author;
        private String message;
        private String messageEn;
        private String timestamp;
        private List<String> attachments;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ResponseAuthor {
            private String name;
            private String role;
            private String avatar;
        }
    }
}