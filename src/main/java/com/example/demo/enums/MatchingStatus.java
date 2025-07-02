package com.example.demo.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum MatchingStatus {
    REQUESTED("신청완료"),
    MATCHED("매칭완료"),
    RESPONDED("답변완료");

    private final String displayName;

    MatchingStatus(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

}