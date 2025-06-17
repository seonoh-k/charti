package com.example.demo.survey.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/surveys")
public class SurveyAdminViewController {

    // 📄 기록 문진 등록/관리 화면
    @GetMapping("/record")
    public String showRecordSurveyForm() {
        log.info("[GET] 관리자 - 기록 문진 등록/관리 화면 요청");
        return "admin/surveys/recordForm"; // 실제 HTML 파일 경로
    }

//    // 📄 데일리 문진 등록/관리 화면
//    @GetMapping("/daily")
//    public String showDailySurveyForm() {
//        log.info("[GET] 관리자 - 데일리 문진 등록/관리 화면 요청");
//        return "admin/surveys/dailyForm";
//    }
//
//    // 📄 특별 문진 등록/관리 화면
//    @GetMapping("/special")
//    public String showSpecialSurveyForm() {
//        log.info("[GET] 관리자 - 특별 문진 등록/관리 화면 요청");
//        return "admin/surveys/specialForm";
//    }

}
