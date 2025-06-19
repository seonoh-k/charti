package com.example.demo.survey.controller;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.enums.TargetGroup;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Users;
import com.example.demo.users.exception.UserNotFoundException;
import com.example.demo.users.service.ChildService;
import com.example.demo.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SpecialSurveyPageController {

    private final UserService  userService;
    private final ChildService childService;

    @GetMapping("/specialSurvey")
    public String showPage(Authentication auth, Model model) {
        // 1) 로그인 사용자 조회
        Users me;
        try {
            me = userService.findByUsernameEntity(auth.getName());
        } catch (UserNotFoundException e) {
            me = userService.findByUuidEntity(auth.getName());
        }

        // 2) 자녀 목록
        List<Child> children = childService.findByUsersId(me.getId());
        model.addAttribute("children", children);

        // 3) 연령대·카테고리 ENUM 목록
        model.addAttribute("ageGroups",
                List.of(AgeGroup.values()).stream()
                        .filter(a->a!=AgeGroup.ALL && a!=AgeGroup.VARIOUS)
                        .toList());
        model.addAttribute("categories",
                List.of(SurveyCategory.values()));

        return "specialSurvey";  // templates/specialSurvey.html
    }

    @GetMapping("/specialSurvey/result")
    public String showResultPage() {
        return "specialSurveyResult";  // templates/specialSurveyResult.html
    }
}
