package com.example.demo.survey.controller;

import com.example.demo.survey.dto.SurveyRequestDto;
import com.example.demo.survey.entity.DailySurvey;
import com.example.demo.survey.service.DailySurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class DailySurveyController {

    private final DailySurveyService dailySurveyService;

    @GetMapping("/{ageGroup}")
    @ResponseBody
    public List<DailySurvey> getSurveyListByAgeGroup(@PathVariable String ageGroup) {
        return dailySurveyService.getSurveysByAgeGroup(ageGroup);
    }

    @PostMapping(value = "/submit", consumes = "application/json; charset=UTF-8", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public Map<String, Object> submitSurvey(@RequestBody SurveyRequestDto dto) {
        System.out.println("====== SurveyRequestDto DTO 확인 ======");
        System.out.println("받은 ageGroup: " + dto.getAgeGroup());
        System.out.println("받은 answers: " + dto.getAnswers());
        System.out.println("===================================");

        List<DailySurvey> surveyList = dailySurveyService.getSurveysByAgeGroup(dto.getAgeGroup());
        if (surveyList == null || surveyList.isEmpty()) {
            throw new IllegalArgumentException("문진 데이터가 없습니다.");
        }

        if (dto.getAnswers() == null || dto.getAnswers().isEmpty()) {
            throw new IllegalArgumentException("답변이 없습니다.");
        }

        if (dto.getAnswers().size() != surveyList.size()) {
            throw new IllegalArgumentException("응답 배열과 설문 데이터 크기가 일치하지 않습니다.");
        }

        double riskScore = dailySurveyService.calculateRiskScore(dto.getAnswers(), surveyList);
        Map<String, Double> categoryScores = dailySurveyService.calculateCategoryRiskScore(dto.getAnswers(), surveyList);

        Map<String, Object> result = new HashMap<>();
        result.put("totalRiskScore", riskScore);
        result.put("categoryScores", categoryScores);

        return result;
    }



    // 결과 페이지 라우팅 추가
    @GetMapping("/result")
    public String resultPage() {
        return "dailySurveyResult"; // templates/dailySurveyResult.html
    }
}