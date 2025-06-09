package com.example.demo.mapper;

import com.example.demo.dto.RecordSurveyRequest;
import com.example.demo.dto.RecordSurveyResponse;
import com.example.demo.entity.RecordSurvey;

public class RecordSurveyMapper {

    public static RecordSurvey toEntity(RecordSurveyRequest request) {
        RecordSurvey entity = new RecordSurvey();
        entity.setQuestion(request.getQuestion());
        return entity;
    }

    public static RecordSurveyResponse toResponse(RecordSurvey entity) {
        return new RecordSurveyResponse(
                entity.getSurveyId(),
                entity.getQuestion(),
                entity.getCreatedAt()
        );
    }
}
