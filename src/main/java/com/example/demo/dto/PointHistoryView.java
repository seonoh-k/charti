package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class PointHistoryView {
    private int beforePoint;       // 변경 전 포인트
    private int afterPoint;        // 변경 후 포인트
    private int changeAmount;      // 증감 수치 (+/-)
    private String description;    // 사유
    private LocalDateTime createdAt; // 생성일
}
