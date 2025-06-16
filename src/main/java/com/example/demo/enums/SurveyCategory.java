package com.example.demo.enums;

public enum SurveyCategory {
    COMMUNICATION("의사소통/정서/심리"),
    MOTOR("운동/인지/발달"),
    SOCIAL("사회성/인지/창의"),
    LIFESTYLE("생활습관");

    private final String label;

    SurveyCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}