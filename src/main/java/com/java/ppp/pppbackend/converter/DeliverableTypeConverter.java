package com.java.ppp.pppbackend.converter;

import com.java.ppp.pppbackend.entity.DeliverableType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DeliverableTypeConverter implements AttributeConverter<DeliverableType, String> {

    @Override
    public String convertToDatabaseColumn(DeliverableType attribute) {
        if (attribute == null) {
            return null;
        }
        // Write to DB as lowercase (matches your SQL check constraints)
        return attribute.name().toLowerCase();
    }

    @Override
    public DeliverableType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            // Read from DB (which is lowercase) and convert to Enum (UPPERCASE)
            return DeliverableType.valueOf(dbData.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Handle unknown values gracefully (optional)
            return null;
        }
    }
}