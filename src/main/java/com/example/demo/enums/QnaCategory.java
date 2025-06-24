package com.example.demo.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum QnaCategory {
    SIGNUP_LOGIN("회원가입/로그인"),
    ACCOUNT("계정/개인정보"),
    SURVEY("문진"),
    PAYMENT_REFUND("결제/환불"),
    PARTNERSHIP("제휴/비즈니스"),
    OTHER("기타 문의"),
    ALL("전체");

    private final String name;

    QnaCategory(String name) { this.name = name; }

    public String toString() { return name; }

    @JsonValue
    public String toJson() { return name; }
}
