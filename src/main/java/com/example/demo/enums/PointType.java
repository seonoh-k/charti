package com.example.demo.enums;

public enum PointType {
    CONSULT("전문가 상담"), // 차감용도 포인트사용처
    RECORD_SURVEY("기록문진"),
    DAILY_SURVEY("데일리문진"),
    EVENT("이벤트"),
    ADMIN("관리자 수동지급");


    private final String label;

    PointType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
