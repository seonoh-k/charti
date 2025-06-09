package com.example.demo.mapper;

import com.example.demo.dto.RecordAnswerRequest;
import com.example.demo.dto.RecordAnswerResponse;
import com.example.demo.entity.*;

public class RecordAnswerMapper {

    public static RecordAnswer toEntity(RecordAnswerRequest dto, RecordSurvey survey, Member writer, Child child) {
        RecordAnswer entity = new RecordAnswer();
        entity.setSurvey(survey);
        entity.setWriter(writer);
        entity.setChild(child);
        entity.setQuestion(survey.getQuestion());
        entity.setAnswer(dto.getAnswer());
        return entity;
    }

    public static RecordAnswerResponse toResponse(RecordAnswer entity) {
        return new RecordAnswerResponse(
                entity.getAnswerId(),
                entity.getQuestion(),
                entity.getAnswer(),
                entity.getChild().getName(),
                entity.getCreatedAt()
        );
    }
}
