package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/survey")
public class SurveyController {

    @GetMapping("/health")
    public String health() {
        return "questionnaire01";
    }

    @GetMapping("/disease")
    public String disease() {
        return "questionnaire02";
    }
}
