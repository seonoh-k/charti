package com.example.demo.survey.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
// 생성·수정 폼 DTO
public class SurveySetForm {
    private Long id;
    @NotBlank(message = "세트명을 입력하세요")
    private String setTitle;
    @NotBlank
    private String type;               // GROUP 또는 SPECIAL
    private List<Long> surveyIds;
}