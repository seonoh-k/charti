package com.example.demo.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum QnaCategory {
    SIGNUP_LOGIN("회원가입/로그인"),
    ACCOUNT("계정/개인정보"),
    SURVEY("문진 관련"),
    GROUP("그룹 관련"),
    MATCHING("상담 관련"),
    PAYMENT_REFUND("결제/환불"),
    PARTNERSHIP("제휴/비즈니스"),
    OTHER("기타 문의"),
    ALL("전체");

    private final String displayName;

    QnaCategory(String displayName) { this.displayName = displayName; }

    public String toString() { return displayName; }

    @JsonValue
    public String toJson() { return displayName; }

    // ALL("전체")를 제외한 모든 카테고리
    public static List<QnaCategory> formValues() {
        List<QnaCategory> list = new ArrayList<>();
        for (QnaCategory qnaCategory : values()) {
            if(qnaCategory != ALL) {
                list.add(qnaCategory);
            }
        }
        return list;
    }
}
