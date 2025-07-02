package com.example.demo.survey.controller;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.enums.TargetGroup;
import com.example.demo.survey.entity.SurveySet;
import com.example.demo.survey.service.SurveySetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/survey-sets")
public class SurveySetApiController {

    private final SurveySetService surveySetService;

    @GetMapping
    public ResponseEntity<List<SurveySet>> getSurveySets(
            @RequestParam String type,
            @RequestParam(required = false) AgeGroup ageGroup,
            @RequestParam(required = false) TargetGroup targetGroup,
            @RequestParam(required = false) SurveyCategory category
    ) {
        List<SurveySet> sets = surveySetService.findByCriteria(type, ageGroup, targetGroup, category);
        return ResponseEntity.ok(sets);
    }
}