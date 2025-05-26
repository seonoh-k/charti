package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Log4j2
public class PatientController {


    @GetMapping("/patientJoinForm")
    public String showPatientJoinForm(){
        log.info("[GET] PatientController.showPatientJoinForm");
        return "patientJoinForm";
    }
    @GetMapping("/patientLoginForm")
    public String showPatientLoginForm(){
        log.info("[GET] PatientController.showPatientLoginForm");
        return "patientLoginForm";
    }
}
