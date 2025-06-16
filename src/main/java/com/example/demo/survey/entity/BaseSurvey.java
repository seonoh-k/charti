package com.example.demo.survey.entity;

/**
 * GroupSurvey와 SpecialSurvey의 공통 속성/메서드를 정의하기 위한 인터페이스
 */
public interface BaseSurvey {
    Long getId();          // 설문 고유 식별자
    String getAgeGroup();  // 연령대 반환
    String getCategory();  // 카테고리 반환
}