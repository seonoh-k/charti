package com.example.demo.survey.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DailyAnswerRequest {
    private int answerValue;   // 클라이언트에서는 { "answerValue": 4 } 처럼 보냄
    // 반드시 getter/setter 추가
    public int getAnswerValue() {
        return answerValue;
    }
    public void setAnswerValue(int answerValue) {
        this.answerValue = answerValue;
    }
}
