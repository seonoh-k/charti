package com.example.demo.converter;

import com.example.demo.enums.AgeGroup;
import org.springframework.stereotype.Component;

@Component
public class AgeGroupRequestConverter implements org.springframework.core.convert.converter.Converter<String, AgeGroup> {
    @Override
    public AgeGroup convert(String source) {
        return AgeGroup.fromValue(source);
    }
}
