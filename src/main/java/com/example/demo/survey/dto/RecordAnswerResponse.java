package com.example.demo.survey.dto;

import com.example.demo.survey.entity.RecordAnswer;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@AllArgsConstructor
public class RecordAnswerResponse {
    private Long answerId;
    private String question;
    private String answer;
    private String childName;
    private LocalDateTime createdAt;

    public static RecordAnswerResponse fromEntity(RecordAnswer entity) {
        return new RecordAnswerResponse(
                entity.getAnswerId(),
                entity.getSurvey().getQuestion(),
                entity.getAnswer(),
                entity.getChild().getName(),
                entity.getCreatedAt()
        );
    }

}
