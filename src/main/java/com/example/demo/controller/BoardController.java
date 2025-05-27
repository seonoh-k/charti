package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BoardController {

    @GetMapping("/board/health")
    public String healthBoardPage() {
        return "healthBoard"; // templates/healthBoard.html
    }
}