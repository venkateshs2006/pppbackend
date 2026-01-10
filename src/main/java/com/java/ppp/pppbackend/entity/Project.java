package com.java.ppp.pppbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "title_ar")
    private String titleAr;

    @Column(name = "title_en", nullable = false)
    private String titleEn;

    @Column(name = "description_ar", columnDefinition = "TEXT")
    private String descriptionAr;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    @Convert(converter = com.java.ppp.pppbackend.converter.ProjectStatusConverter.class)
    private ProjectStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProjectPriority priority;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

      // ✅ FIX: Remove 'default 0.00' from columnDefinition
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal budget = BigDecimal.ZERO;

    // ✅ FIX: Remove 'default 0.00' from columnDefinition
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal spent = BigDecimal.ZERO;

    // ✅ FIX: Remove 'default 0' SQL definition
    @Builder.Default
    private Integer progress = 0;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    // --- Relationships ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    @ToString.Exclude
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_manager_id")
    @ToString.Exclude
    private User projectManager;

    @OneToMany( mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<ProjectMember> members;

    // This stores the list of strings in a separate joined table automatically
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<Deliverable> deliverables;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<ProjectFile> files;
}