package com.example.demo.survey.dto;

import com.example.demo.survey.entity.RecordSurvey;
import com.example.demo.users.entity.Child;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
public class RecordSurveyDataResponse {

    private final Child child;
    private final List<RecordSurvey> surveys;

    public RecordSurveyDataResponse(Child child, List<RecordSurvey> surveys) {
        this.child = child;
        this.surveys = surveys;
    }
}