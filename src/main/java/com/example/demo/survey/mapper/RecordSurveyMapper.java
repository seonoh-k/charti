package com.example.demo.survey.mapper;

import com.example.demo.survey.dto.RecordSurveyRequest;
import com.example.demo.survey.dto.RecordSurveyResponse;
import com.example.demo.survey.entity.RecordSurvey;

public class RecordSurveyMapper {

    public static RecordSurvey toEntity(RecordSurveyRequest request) {
        RecordSurvey entity = new RecordSurvey();
        entity.setAgeGroup(request.getAgeGroup());
        entity.setQuestion(request.getQuestion());
        entity.setAnswer("");
        return entity;
    }

    public static RecordSurveyResponse toResponse(RecordSurvey entity) {
        return new RecordSurveyResponse(
                entity.getSurveyId(),
                entity.getQuestion(),
                entity.getAgeGroup(),
                entity.getCreatedAt()
        );
    }
}
