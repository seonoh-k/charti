package com.example.demo.controller;

import com.example.demo.users.service.FirebaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Log4j2
public class FirebaseController {

    private final FirebaseService firebaseService;

}
