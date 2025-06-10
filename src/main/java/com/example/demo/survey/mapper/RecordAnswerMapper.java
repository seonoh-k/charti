package com.example.demo.survey.mapper;

import com.example.demo.survey.dto.RecordAnswerRequest;
import com.example.demo.survey.dto.RecordAnswerResponse;
import com.example.demo.survey.entity.RecordAnswer;
import com.example.demo.survey.entity.RecordSurvey;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Member;

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
