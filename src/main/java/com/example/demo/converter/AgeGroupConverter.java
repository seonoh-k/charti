package com.example.demo.converter;

import com.example.demo.enums.AgeGroup;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AgeGroupConverter implements AttributeConverter<AgeGroup, String> {
    @Override
    public String convertToDatabaseColumn(AgeGroup attribute) {
        return attribute == null ? null : attribute.getDisplayName();
    }

    @Override
    public AgeGroup convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return AgeGroup.fromValue(dbData);
    }
}