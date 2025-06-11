package com.example.demo.controller;

import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/daily-survey")
    public String getDailySurveyPage() {
        return "layout/DailySurvey";
    }

    @GetMapping("/daily-survey-result")
    public String getSurveyResultPage() {
        return "layout/DailySurveyResult";
    }

    // 아래 두개는 테스트용
    @GetMapping("/point")
    public String getUserPointPage() {
        return "test-point";
    }

    @GetMapping("/admin/point")
    public String getAdminPointPage() {
        return "test-point-admin";
    }

    @GetMapping("/albums")
    public String getAlbumsPage() { return "albums";}

    @GetMapping("/admin/record-survey")
    public String getAdminRecordSurveyPage() {
        return "test-record-admin";
    }

}
