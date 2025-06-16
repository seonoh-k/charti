package com.example.demo.survey.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
// 검색·필터용 DTO
public class SurveySetSearchDto {
    private String keyword  = "";
    private String ageGroup = "all";  // all, 0~12,1~2,3~4,5
    private String category = "all";  // all, 의사소통,인지/운동,생활습관,정서/사회성
    private String type     = "all";  // all, GROUP,SPECIAL
}