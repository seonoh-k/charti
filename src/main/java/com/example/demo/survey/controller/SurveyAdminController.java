package com.example.demo.survey.controller;

import com.example.demo.survey.entity.DailySurvey;
import com.example.demo.survey.service.DailySurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin/surveys")
@RequiredArgsConstructor
public class SurveyAdminController {
    private final DailySurveyService surveyService;

    // 관리 페이지에서 고정으로 사용할 연령대 목록
    private final List<String> ageGroups = Arrays.asList("0~12개월", "1~2세", "3~4세", "5세");
    private final DailySurveyService dailySurveyService;

    // 1. 설문 리스트 (연령대, 카테고리 필터링)
    @GetMapping({"", "/list"})
    public String list(
            @RequestParam(defaultValue = "0~12개월") String ageGroup,
            @RequestParam(defaultValue = "") String category,
            Model model
    ) {
        List<String> categories = surveyService.getDistinctCategories();
        List<DailySurvey> surveys;

        if (category == null || category.isBlank()) {
            surveys = surveyService.getSurveysByAgeGroup(ageGroup);
        } else {
            surveys = surveyService.getSurveysByAgeAndCategory(ageGroup, category);
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
        model.addAttribute("categories", surveyService.getDistinctCategories());
        return "admin/surveys/dailyForm";
    }

    // 2. 설문 저장 처리
    @PostMapping
    public String create(@ModelAttribute DailySurvey survey) {
        surveyService.save(survey);
        return "redirect:/admin/surveys";
    }

    // 3. 설문 수정 폼
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        DailySurvey survey = surveyService.findById(id);
        model.addAttribute("survey", survey);
        model.addAttribute("ageGroups", ageGroups);
        model.addAttribute("categories", surveyService.getDistinctCategories());
        return "admin/surveys/dailyForm";
    }

    // 3. 설문 수정 처리
    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @ModelAttribute DailySurvey formData
    ) {
        DailySurvey survey = surveyService.findById(id);
        survey.setAgeGroup(formData.getAgeGroup());
        survey.setCategory(formData.getCategory());
        survey.setQuestion(formData.getQuestion());
        survey.setWeight(formData.getWeight());
        survey.setAnswer1(formData.getAnswer1());
        survey.setAnswer2(formData.getAnswer2());
        survey.setAnswer3(formData.getAnswer3());
        survey.setAnswer4(formData.getAnswer4());
        survey.setAnswer5(formData.getAnswer5());
        surveyService.save(survey);
        return "redirect:/admin/surveys";
    }

    // 3. 설문 삭제 처리
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        surveyService.delete(id);
        return "redirect:/admin/surveys";
    }
}
