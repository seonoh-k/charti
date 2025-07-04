package com.example.demo.survey.controller;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.enums.TargetGroup;
import com.example.demo.survey.entity.DailySurvey;
import com.example.demo.survey.entity.GroupAnswer;
import com.example.demo.survey.entity.GroupSurvey;
import com.example.demo.survey.service.GroupAnswerService;
import com.example.demo.survey.service.GroupSurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/surveys/group")
@RequiredArgsConstructor
public class GroupSurveyAdminController {

    private final GroupSurveyService groupSurveyService;
    private final List<AgeGroup> ageGroups = Arrays.stream(AgeGroup.values())
            .filter(ag -> ag != AgeGroup.ALL && ag != AgeGroup.VARIOUS)
            .collect(Collectors.toList());
    private final List<SurveyCategory> allCategories = Arrays.stream(SurveyCategory.values())
            .filter(sc -> sc != SurveyCategory.ALL && sc != SurveyCategory.VARIOUS)
            .collect(Collectors.toList());

    private final List<TargetGroup> targetGroups = Arrays.stream(TargetGroup.values())
            .filter(sc -> sc != TargetGroup.ALL)
            .collect(Collectors.toList());

    private final GroupAnswerService groupAnswerService;

    // 1. 설문 리스트 (연령대, 카테고리 필터링)
    @GetMapping({"", "/list"})
    public String list(
            @RequestParam(defaultValue = "ALL") AgeGroup ageGroup,
            @RequestParam(defaultValue = "ALL") SurveyCategory category,
            @RequestParam(defaultValue = "ALL") TargetGroup targetGroup,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            Model model
    ) {
        // service 쪽에서 distinct 카테고리를 enum 리스트로 반환하도록 수정했다면
        List<SurveyCategory> categories = groupSurveyService.getDistinctCategories();

        Page<GroupSurvey> surveys = groupSurveyService.findByFilters(
                ageGroup, category, targetGroup, pageable);

        int total = surveys.getTotalPages();
        int curr  = surveys.getNumber();
        int window = 10;

        int startPage = Math.max(0, curr - window/2);
        int endPage   = Math.min(total-1, startPage + window - 1);
        if (endPage - startPage < window -1) {
            startPage = Math.max(0, endPage - window + 1);
        }

        model.addAttribute("surveys",   surveys);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage",   endPage);

        model.addAttribute("ageGroup", ageGroup);
        model.addAttribute("category", category);
        model.addAttribute("targetGroup",  targetGroup);
        model.addAttribute("ageGroups", ageGroups);
        model.addAttribute("categories", categories);
        model.addAttribute("targetGroups", targetGroups);
        model.addAttribute("surveys", surveys);
        return "admin/surveys/groupList";
    }

    // 2. 설문 작성 폼
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("survey", new GroupSurvey());
        model.addAttribute("ageGroups", ageGroups);
        model.addAttribute("categories", allCategories);
        model.addAttribute("targetGroups", targetGroups);
        return "admin/surveys/groupForm";
    }

    // 2. 설문 저장 처리
    @PostMapping
    public String create(@ModelAttribute GroupSurvey survey) {
        groupSurveyService.save(survey);
        return "redirect:/admin/surveys/group";
    }

    // 3. 설문 수정 폼
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        GroupSurvey survey = groupSurveyService.findById(id);
        model.addAttribute("survey", survey);
        model.addAttribute("ageGroups", ageGroups);
        model.addAttribute("categories", allCategories);
        model.addAttribute("targetGroups", targetGroups);
        return "admin/surveys/groupForm";
    }

    // 3. 설문 수정 처리
    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @ModelAttribute GroupSurvey formData
    ) {
        GroupSurvey survey = groupSurveyService.findById(id);
        // formData.getAgeGroup() 은 이제 AgeGroup enum
        survey.setAgeGroup(formData.getAgeGroup());
        survey.setCategory(formData.getCategory());
        survey.setTargetGroup(formData.getRawTargetGroup());
        survey.setQuestion(formData.getQuestion());
        survey.setAnswer1(formData.getAnswer1());
        survey.setAnswer2(formData.getAnswer2());
        survey.setAnswer3(formData.getAnswer3());
        survey.setAnswer4(formData.getAnswer4());
        survey.setAnswer5(formData.getAnswer5());
        groupSurveyService.save(survey);
        return "redirect:/admin/surveys/group";
    }

    // 3. 설문 삭제 처리
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        groupSurveyService.delete(id);
        return "redirect:/admin/surveys/group";
    }

}
