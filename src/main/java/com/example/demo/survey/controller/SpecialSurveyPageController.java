package com.example.demo.survey.controller;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
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
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/specialSurvey") // [수정] 일관성을 위해 클래스 레벨에 @RequestMapping 추가
@RequiredArgsConstructor
public class SpecialSurveyPageController {

    private final UserService  userService;
    private final ChildService childService;

    /**
     * [수정] 기존의 모든 카테고리를 선택할 수 있는 일반 특별 문진 페이지를 보여줍니다.
     * 경로: GET /specialSurvey
     */
    @GetMapping
    public String showPage(Authentication auth, Model model) {
        Users me = findCurrentUser(auth);

        model.addAttribute("children", childService.findByUsersId(me.getId()));
        model.addAttribute("ageGroups", AgeGroup.values());
        model.addAttribute("categories", SurveyCategory.values());

        return "specialSurvey";
    }

    /**
     * [기존 유지] 특별 문진 결과 페이지를 보여줍니다.
     * 경로: GET /specialSurvey/result
     */
    @GetMapping("/result")
    public String showResultPage() {
        return "specialSurveyResult";
    }

    /**
     * [추가된 메소드]
     * 위험군 타겟 특별 문진 페이지('/specialSurvey/by-risk')를 보여줍니다.
     * 레이아웃에 필요한 'children' 데이터를 모델에 추가합니다.
     */
    @GetMapping("/by-risk")
    public String showTargetedSurveyPage(Authentication auth, Model model) {
        Users me = findCurrentUser(auth);
        model.addAttribute("children", childService.findByUsersId(me.getId()));

        return "survey/specialSurveyByRisk";
    }

    // 위험군 특별 문진 '결과' 페이지
    @GetMapping("/result/by-risk")
    public String showTargetedSurveyResultPage() {
        return "survey/specialAnswerByRisk"; // 2단계에서 만든 html 파일
    }


    private Users findCurrentUser(Authentication auth) {
        try {
            return userService.findByUsernameEntity(auth.getName());
        } catch (UserNotFoundException e) {
            return userService.findByUuidEntity(auth.getName());
        }
    }
}
