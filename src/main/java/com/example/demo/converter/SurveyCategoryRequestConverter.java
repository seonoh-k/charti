package com.example.demo.converter;

import com.example.demo.enums.SurveyCategory;
import org.springframework.stereotype.Component;

@Component
public class SurveyCategoryRequestConverter implements org.springframework.core.convert.converter.Converter<String, SurveyCategory> {
    @Override
    public SurveyCategory convert(String source) {
        return SurveyCategory.fromValue(source);
    }
}