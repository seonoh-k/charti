package com.example.demo.enums;

public enum AgeGroup {
    AGE_0_12("0~12개월"),
    AGE_1_2("1~2세"),
    AGE_3_4("3~4세"),
    AGE_5("5세");

    private final String label;

    AgeGroup(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}