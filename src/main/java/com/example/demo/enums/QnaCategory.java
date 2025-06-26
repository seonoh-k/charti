package com.example.demo.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum QnaCategory {
    SIGNUP_LOGIN("회원가입/로그인"),
    ACCOUNT("계정/개인정보"),
    SURVEY("문진 관련"),
    GROUP("그룹 관련"),
    MATCHING("상담 관련"),
    PAYMENT_REFUND("결제/환불"),
    PARTNERSHIP("제휴/비즈니스"),
    OTHER("기타 문의");

    private final String displayName;

    QnaCategory(String displayName) { this.displayName = displayName; }

    public String toString() { return displayName; }

    @JsonValue
    public String toJson() { return displayName; }

}
