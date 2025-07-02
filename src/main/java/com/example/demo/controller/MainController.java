package com.example.demo.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.users.entity.Role;
import com.example.demo.users.service.AuthService;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {

    private final AuthService authService;

    @GetMapping("/")
    public String index() { return "index"; }

    @GetMapping( "/main")
    public String toMain() { return "redirect:/role"; }

    @GetMapping("/role")
    public String role() {
        UserDTO userDTO = authService.getLoginUser();
        Role role = Role.valueOf(userDTO.getRole());

        return switch (role) {
            case ROLE_MEMBER -> "index";
            case ROLE_EXPERT -> "redirect:/expert/main";
            case ROLE_MANAGER -> "redirect:/manager/main";
            default -> "redirect:/error";
        };
    }

    @GetMapping("/dailySurvey/result")
    public String getSurveyResultPage() {
        return "dailySurveyResult";
    }

//    @GetMapping("/admin/surveys/special")
//    public String manageSpecialSurveyPage() {
//        return "/admin/surveys/manageSpecialSurvey";
//    }

//    @GetMapping("/admin/surveys/group")
//    public String manageGroupSurveyPage() {
//        return "/admin/surveys/manageGroupSurvey";
//    }

    @GetMapping("/survey/history")
    public String surveyHistoryPage() { return  "surveyHistory"; }

    @GetMapping("/about")
    public String aboutPage() { return  "about"; }
}
