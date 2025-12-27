package com.java.ppp.pppbackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 100)
    private String slug;

    @Column(length = 200)
    private String description;

    @Column(name = "usage_count")
    @Builder.Default
    private Long usageCount = 0L;

    @Column(name = "created_at")
    private Timestamp createdAt;
}

