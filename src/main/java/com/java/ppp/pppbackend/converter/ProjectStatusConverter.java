package com.java.ppp.pppbackend.converter;

import com.java.ppp.pppbackend.entity.ProjectStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ProjectStatusConverter implements AttributeConverter<ProjectStatus, String> {

    @Override
    public String convertToDatabaseColumn(ProjectStatus attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public ProjectStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ProjectStatus.fromDbValue(dbData);
    }
}