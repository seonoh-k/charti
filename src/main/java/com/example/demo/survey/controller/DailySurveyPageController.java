package com.example.demo.survey.controller;

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
public class DailySurveyPageController {

    private final UserService  userService;
    private final ChildService childService;

    @GetMapping("/dailySurvey")
    public String showDailySurveyPage(Authentication authentication, Model model) {
        String principalName = authentication.getName();
        Users me;

        // 1) 우선 principalName 이 username(이메일) 일 경우 조회 시도
        try {
            me = userService.findByUsernameEntity(principalName);
        }
        // 2) 실패(UserNotFoundException) 하면 principalName 을 UUID 로 간주
        catch (UserNotFoundException e) {
            me = userService.findByUuidEntity(principalName);
        }

        // 3) 올바른 사용자의 자녀 목록 조회
        List<Child> children = childService.findByUsersId(me.getId());
        model.addAttribute("children", children);

        return "dailySurvey";
    }
}
