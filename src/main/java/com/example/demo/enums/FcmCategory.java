package com.example.demo.enums;

public enum FcmCategory {
    DAILY("데일리문진"),
    SPECIAL("특별문진"),
    NOTICE("공지사항"),
    SYSTEM("시스템알림");

    private final String label;

    FcmCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}