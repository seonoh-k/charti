package com.example.demo.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AgeGroup {
    AGE_0_12("0~12개월"),
    AGE_1_2("1~2세"),
    AGE_3_4("3~4세"),
    AGE_5("5세"),
    // 검색용 전체(all)
    ALL("all"),

    // 세트 저장용 통합(various)
    VARIOUS("various");

    private final String displayName;

    AgeGroup(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static AgeGroup fromValue(String value) {
        if (value == null) throw new IllegalArgumentException("value is null");
        String normalized = value.trim(); // ← 공백 제거
        for (AgeGroup ag : values()) {
            if (ag.displayName.equals(normalized) || ag.name().equalsIgnoreCase(normalized)) {
                return ag;
            }
        }
        throw new IllegalArgumentException("Unknown AgeGroup: " + value);
    }

}