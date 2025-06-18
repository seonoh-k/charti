package com.example.demo.survey.entity;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.enums.TargetGroup;

import java.util.Optional;

/**
 * GroupSurvey와 SpecialSurvey의 공통 속성/메서드를 정의하기 위한 인터페이스
 */
public interface BaseSurvey {
    Long getId();          // 설문 고유 식별자
    AgeGroup getAgeGroup();  // 연령대 반환

    default Optional<TargetGroup> getTargetGroup() {
        return Optional.empty();
    }
    SurveyCategory getCategory();  // 카테고리 반환



}