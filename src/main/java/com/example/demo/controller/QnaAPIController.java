package com.example.demo.controller;

import com.example.demo.service.QnaService;
import com.example.demo.users.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class QnaAPIController {

    private final AuthService authService;
    private final QnaService qnaService;
}
