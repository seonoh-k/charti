package com.example.demo.survey.controller;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.survey.entity.BaseSurvey;
import com.example.demo.survey.service.SurveySetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class SurveyApiController {
    private final SurveySetService service;

    @GetMapping
    public List<? extends BaseSurvey> getSurveys(
            @RequestParam String type,
            @RequestParam(defaultValue = "ALL") AgeGroup age,
            @RequestParam(defaultValue = "ALL") SurveyCategory category
    ) {
        return "SPECIAL".equals(type)
                ? service.allSpecial(age, category)
                : service.allGroup(age, category);
    }
}