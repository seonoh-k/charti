package com.example.demo.survey.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AnswerUpdateRequest(
        @JsonProperty("answerValue") int answerValue
) {}