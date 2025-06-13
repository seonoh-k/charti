package com.example.demo.users.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Log4j2
public class AdminController {

    @GetMapping("/admin")
    public String showAdminPage() {
        log.info("[GET] 👨‍💼 request Admin Page");
        return "admin";
    }

}
