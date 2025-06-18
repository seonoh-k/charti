package com.example.demo.survey.controller;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.survey.entity.DailySurvey;
import com.example.demo.survey.service.DailySurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/surveys/daily")
@RequiredArgsConstructor
public class DailySurveyAdminController {

    private final DailySurveyService dailySurveyService;
    private final List<AgeGroup> ageGroups = Arrays.stream(AgeGroup.values())
            .filter(ag -> ag != AgeGroup.ALL && ag != AgeGroup.VARIOUS)
            .collect(Collectors.toList());
    private final List<SurveyCategory> allCategories = Arrays.stream(SurveyCategory.values())
            .filter(sc -> sc != SurveyCategory.ALL && sc != SurveyCategory.VARIOUS)
            .collect(Collectors.toList());

    // 1. 설문 리스트 (연령대, 카테고리 필터링)
    @GetMapping({"", "/list"})
    public String list(
            // defaultValue는 enum 이름을 사용합니다
            @RequestParam(defaultValue = "ALL") AgeGroup ageGroup,
            @RequestParam(defaultValue = "ALL") SurveyCategory category,
            Model model
    ) {
        // service 쪽에서 distinct 카테고리를 enum 리스트로 반환하도록 수정했다면
        List<SurveyCategory> categories = dailySurveyService.getDistinctCategories();

        List<DailySurvey> surveys;
        if (ageGroup == AgeGroup.ALL && category == SurveyCategory.ALL) {
            // 전체
            surveys = dailySurveyService.findAllSurveys();
        } else if (ageGroup != AgeGroup.ALL && category == SurveyCategory.ALL) {
            // 연령대만 필터
            surveys = dailySurveyService.getSurveysByAgeGroup(ageGroup);
        } else if (ageGroup == AgeGroup.ALL && category != SurveyCategory.ALL) {
            // 카테고리만 필터
            surveys = dailySurveyService.getSurveysByCategory(category);
        } else {
            // 둘 다 필터
            surveys = dailySurveyService.getSurveysByAgeAndCategory(ageGroup, category);
        }

        model.addAttribute("ageGroup", ageGroup);
        model.addAttribute("category", category);
        model.addAttribute("ageGroups", ageGroups);
        model.addAttribute("categories", categories);
        model.addAttribute("surveys", surveys);
        return "admin/surveys/dailyList";
    }

    // 2. 설문 작성 폼
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("survey", new DailySurvey());
        model.addAttribute("ageGroups", ageGroups);
        model.addAttribute("categories", allCategories);
        return "admin/surveys/dailyForm";
    }

    // 2. 설문 저장 처리
    @PostMapping
    public String create(@ModelAttribute DailySurvey survey) {
        dailySurveyService.save(survey);
        return "redirect:/admin/surveys";
    }

    // 3. 설문 수정 폼
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        DailySurvey survey = dailySurveyService.findById(id);
        model.addAttribute("survey", survey);
        model.addAttribute("ageGroups", ageGroups);
        model.addAttribute("categories", allCategories);
        return "admin/surveys/dailyForm";
    }

    // 3. 설문 수정 처리
    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @ModelAttribute DailySurvey formData
    ) {
        DailySurvey survey = dailySurveyService.findById(id);
        // formData.getAgeGroup() 은 이제 AgeGroup enum
        survey.setAgeGroup(formData.getAgeGroup());
        survey.setCategory(formData.getCategory());
        survey.setQuestion(formData.getQuestion());
        survey.setWeight(formData.getWeight());
        survey.setAnswer1(formData.getAnswer1());
        survey.setAnswer2(formData.getAnswer2());
        survey.setAnswer3(formData.getAnswer3());
        survey.setAnswer4(formData.getAnswer4());
        survey.setAnswer5(formData.getAnswer5());
        dailySurveyService.save(survey);
        return "redirect:/admin/surveys";
    }

    // 3. 설문 삭제 처리
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        dailySurveyService.delete(id);
        return "redirect:/admin/surveys";
    }
}
