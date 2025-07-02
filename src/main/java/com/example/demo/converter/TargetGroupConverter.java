package com.example.demo.converter;

import com.example.demo.enums.TargetGroup;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;

@Converter(autoApply = true)
public class TargetGroupConverter implements AttributeConverter<TargetGroup, String> {

    @Override
    public String convertToDatabaseColumn(TargetGroup attribute) {
        return attribute != null
                ? attribute.getDisplayName()
                : null;
    }

    @Override
    public TargetGroup convertToEntityAttribute(String dbData) {
        // 1) null 이거나 공백인 경우 null 리턴
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        // 2) 실제 enum 값 매핑
        return Arrays.stream(TargetGroup.values())
                .filter(e -> e.getDisplayName().equalsIgnoreCase(dbData))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unknown TargetGroup DB Value: " + dbData));
    }
}
