package com.example.demo.controller;


import groovy.util.logging.Log4j2;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Log4j2
public class QuestionController {

    @GetMapping("/historyQuestion")
    public String write() {
        return "historyQuestion";
    }
}
