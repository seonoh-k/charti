package com.example.demo.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SurveyCategory {
    COMMUNICATION("의사소통/정서/심리"),
    MOTOR_COGNITIVE("운동/인지 발달"),
    SOCIAL_COGNITIVE("사회성/인지/창의"),
    LIFESTYLE("생활습관"),
    // 검색용 전체(all)
    ALL("all"),

    // 세트 저장용 통합(various)
    VARIOUS("various");

    private final String displayName;

    SurveyCategory(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static SurveyCategory fromValue(String value) {
        for (SurveyCategory sc : values()) {
            if (sc.displayName.equals(value) || sc.name().equalsIgnoreCase(value)) {
                return sc;
            }
        }
        throw new IllegalArgumentException("Unknown SurveyCategory: " + value);
    }
}