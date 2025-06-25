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

    @GetMapping("/dailySurvey/result")
    public String getSurveyResultPage() {
        return "dailySurveyResult";
    }

    // 아래 두개는 테스트용
    @GetMapping("/point")
    public String getUserPointPage() {
        return "test-point";
    }


    @GetMapping("/albums/create")
    public String getAlbumsPage() { return "albumsCreate";}

    @GetMapping("/admin/record-survey")
    public String getAdminRecordSurveyPage() {
        return "test-record-admin";
    }


    @GetMapping("/admin/surveys/special")
    public String manageSpecialSurveyPage() {
        return "manage-specialSurvey";
    }

    @GetMapping("/admin/surveys/group")
    public String manageGroupSurveyPage() {
        return "manage-groupSurvey"; // manage-groupSurvey.html 파일의 위치
    }
}
