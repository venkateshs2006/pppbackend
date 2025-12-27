package com.java.ppp.pppbackend.converter;

import com.java.ppp.pppbackend.entity.TicketStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TicketStatusConverter implements AttributeConverter<TicketStatus, String> {
    @Override
    public String convertToDatabaseColumn(TicketStatus attribute) {
        return attribute == null ? null : attribute.getDbValue();
    }

    @Override
    public TicketStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : TicketStatus.fromDbValue(dbData);
    }
}