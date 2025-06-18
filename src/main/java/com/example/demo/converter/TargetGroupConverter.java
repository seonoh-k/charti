package com.example.demo.converter;


import com.example.demo.enums.TargetGroup;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TargetGroupConverter implements AttributeConverter<TargetGroup, String> {

    @Override
    public String convertToDatabaseColumn(TargetGroup attribute) {
        return attribute == null ? null : attribute.getDisplayName();
    }

    @Override
    public TargetGroup convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;


        for (TargetGroup tg : TargetGroup.values()) {
            if (dbData.startsWith(tg.getDisplayName())) {
                return tg;
            }
        }

        throw new IllegalArgumentException("Unknown TargetGroup DB Value: " + dbData);
    }
}

