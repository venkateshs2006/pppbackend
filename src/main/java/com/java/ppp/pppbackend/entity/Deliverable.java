package com.java.ppp.pppbackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "deliverables")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deliverable {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String titleEn;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String descriptionEn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliverableType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliverableStatus status;

    private String version; // e.g., "1.0", "1.1"

    private Integer orderIndex;

    // Hierarchy for tree structure (Guide -> Topic -> Policy)
    @Column(name = "parent_id")
    private UUID parentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false) // This creates the FK column
    @ToString.Exclude // Prevent infinite loop
    @EqualsAndHashCode.Exclude
    @JsonIgnore // Prevent infinite JSON recursion
    private Project project;

    // File info
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String fileType;
    private Long createdById;
    private String createdByName;

    // Assignment for approval
    private Long assignedToId; // ID of the client user
    private String assignedToName;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = DeliverableStatus.DRAFT;
        if (this.version == null) this.version = "1.0";
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}