package com.java.ppp.pppbackend.converter;


import com.java.ppp.pppbackend.entity.RoleType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RoleTypeConverter implements AttributeConverter<RoleType, String> {

    @Override
    public String convertToDatabaseColumn(RoleType attribute) {
        return attribute == null ? null : attribute.getDbValue();
    }

    @Override
    public RoleType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : RoleType.fromDbValue(dbData);
    }
}
