package com.example.demo.controller;

import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {

    @GetMapping({"/", "/main"})
    public String index() {
        return "index";
    }

    @GetMapping("/dailySurvey/result")
    public String getSurveyResultPage() {
        return "dailySurveyResult";
    }

    @GetMapping("/admin/surveys/special")
    public String manageSpecialSurveyPage() {
        return "/admin/surveys/manageSpecialSurvey";
    }

    @GetMapping("/admin/surveys/group")
    public String manageGroupSurveyPage() {
        return "/admin/surveys/manageGroupSurvey";
    }

    @GetMapping("/survey/history")
    public String surveyHistoryPage() { return  "surveyHistory"; }

    @GetMapping("/about")
    public String aboutPage() { return  "about"; }
}
