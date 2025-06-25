package com.example.demo.survey.controller;

import com.example.demo.survey.entity.BaseSurvey;
import com.example.demo.survey.entity.SurveySet;
import com.example.demo.survey.service.SurveySetService;
import com.example.demo.users.entity.Child;
import com.example.demo.users.exception.UserNotFoundException;
import com.example.demo.users.service.ChildService;
import com.example.demo.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/surveySet/request")
@RequiredArgsConstructor
public class SurveySetRequestController {

    private final SurveySetService surveySetService;
    private final ChildService childService;
    private final UserService userService;

    @GetMapping("/{setId}")
    public String showSurveySetRequest(
            @PathVariable Long setId,
            @RequestParam Long childId,
            Authentication auth,
            Model model) {

        // 1) 현재 로그인 사용자 검증 (필요시)
        try {
            userService.findByUsernameEntity(auth.getName());
        } catch (UserNotFoundException e) {
            userService.findByUuidEntity(auth.getName());
        }

        // 2) 자녀 정보 조회
        Child child = childService.findById(childId);
        model.addAttribute("child", child);

        // 3) SurveySet 정보 조회
        SurveySet set = surveySetService.getById(setId);
        model.addAttribute("surveySet", set);

        // 4) 연령대·카테고리·타입 문자열 변환
        model.addAttribute("ageGroup", set.getAgeGroup().getDisplayName());
        model.addAttribute("category", set.getCategory().getDisplayName());
        model.addAttribute("typeLabel",
                "GROUP".equalsIgnoreCase(set.getType()) ? "그룹 문진" : "특별 문진"
        );

        // 5) 실제 문항 리스트
        List<? extends BaseSurvey> questions =
                "GROUP".equalsIgnoreCase(set.getType())
                        ? set.getGroupSurveys()
                        : set.getSpecialSurveys();
        model.addAttribute("questions", questions);

        return "surveySetRequest";  // → templates/surveySetRequest.html
    }

}
