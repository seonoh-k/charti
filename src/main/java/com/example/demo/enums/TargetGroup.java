package com.example.demo.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TargetGroup {
    KINDERGARTEN("유치원"),
    DAYCARE("어린이집"),
    CHILDRENS_HOME("보육원"),
    // 검색용 전체(all)
    ALL("all"),

    // 세트 저장용 통합(various)
    VARIOUS("various");

    private final String displayName;


    TargetGroup(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static TargetGroup fromValue(String value) {
        for(TargetGroup tg : values()) {
            if(tg.displayName.equals(value) || tg.name().equalsIgnoreCase(value)) {
                return tg;
            }
        }
        throw new IllegalArgumentException("Unknown TargetGroup:" + value);
    }
}


