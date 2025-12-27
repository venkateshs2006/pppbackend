package com.java.ppp.pppbackend.converter;

import com.java.ppp.pppbackend.entity.DeliverableType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DeliverableTypeConverter implements AttributeConverter<DeliverableType, String> {

    @Override
    public String convertToDatabaseColumn(DeliverableType attribute) {
        return attribute == null ? null : attribute.getDbValue();
    }

    @Override
    public DeliverableType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : DeliverableType.fromDbValue(dbData);
    }
}
