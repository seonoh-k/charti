package com.example.demo.converter;

import com.example.demo.enums.SurveyCategory;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SurveyCategoryConverter implements AttributeConverter<SurveyCategory, String> {
    @Override
    public String convertToDatabaseColumn(SurveyCategory attribute) {
        return attribute == null ? null : attribute.getDisplayName();
    }

    @Override
    public SurveyCategory convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return SurveyCategory.fromValue(dbData);
    }
}