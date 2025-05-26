package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Log4j2
public class DoctorController {

    @GetMapping("/doctorJoinForm")
    public String showDoctorJoinForm(){
        log.info("[GET] DoctorController.showDoctorJoinForm");
        return "doctorJoinForm";
    }
    @GetMapping("/doctorLoginForm")
    public String showDoctorLoginForm(){
        log.info("[GET] DoctorController.showDoctorLoginForm");
        return "doctorLoginForm";
    }

}
