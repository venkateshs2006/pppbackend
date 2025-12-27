package com.java.ppp.pppbackend.repository;

import com.java.ppp.pppbackend.entity.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
    Optional<EmailTemplate> findByName(String name);

    Optional<EmailTemplate> findByNameAndIsActive(String name, Boolean isActive);
}
