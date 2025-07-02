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

    @GetMapping({"/", "/main"})
    public String index() {
        UserDTO loginUser = authService.getLoginUser();
        Role role = Role.valueOf(loginUser.getRole());

        // ✅ 권한에 따라 마이페이지 리다이렉트
        return switch (role) {
            case ROLE_MEMBER -> "redirect:index";
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
