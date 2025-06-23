package com.example.demo.survey.controller;

import com.example.demo.enums.AgeGroup;
import com.example.demo.survey.entity.RecordSurvey;
import com.example.demo.survey.service.RecordSurveyService;
import com.example.demo.dto.PagingDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/surveys/record")
@RequiredArgsConstructor
public class RecordSurveyAdminController {

    private final RecordSurveyService recordSurveyService;
    private final List<AgeGroup> ageGroups = Arrays.stream(AgeGroup.values())
            .filter(ag -> ag != AgeGroup.ALL && ag != AgeGroup.VARIOUS)
            .collect(Collectors.toList());

    // 1. 기록문진 리스트 (연령대 + 페이징 필터링)
    @GetMapping({"", "/list"})
    public String list(@RequestParam(defaultValue = "ALL") AgeGroup ageGroup,
                       @ModelAttribute PagingDTO<RecordSurvey> pagingDto,
                       Model model) {

        Pageable pageable = pagingDto.toPageable();
        // Pageable pageable = pagingDto.toZeroBasedPageable();
        Page<RecordSurvey> page = (ageGroup == AgeGroup.ALL)
                ? recordSurveyService.findAll(pageable)
                : recordSurveyService.findByAgeGroup(ageGroup, pageable);

        pagingDto.setData(page.getContent());

        model.addAttribute("pagingDto", pagingDto);
        model.addAttribute("selectedAgeGroup", ageGroup.name());
        model.addAttribute("ageGroups", ageGroups);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("currentPage", page.getNumber() + 1);
        return "admin/surveys/recordList";
    }


    // 2. 문진 등록 폼
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("survey", new RecordSurvey());
        model.addAttribute("ageGroups", ageGroups);
        return "admin/surveys/recordForm";
    }

    // 2. 문진 저장 처리
    @PostMapping
    public String create(@Valid @ModelAttribute RecordSurvey survey,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("ageGroups", ageGroups);
            return "admin/surveys/recordForm";
        }
        recordSurveyService.save(survey);
        return "redirect:/admin/surveys/record";
    }

    // 3. 문진 수정 폼
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        RecordSurvey survey = recordSurveyService.findById(id);
        model.addAttribute("survey", survey);
        model.addAttribute("ageGroups", ageGroups);
        return "admin/surveys/recordForm";
    }

    // 3. 문진 수정 처리
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute RecordSurvey formData,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("ageGroups", ageGroups);
            return "admin/surveys/recordForm";
        }

        RecordSurvey survey = recordSurveyService.findById(id);
        survey.setAgeGroup(formData.getAgeGroup());
        survey.setQuestion(formData.getQuestion());
        survey.setAnswer(formData.getAnswer());
        recordSurveyService.save(survey);

        return "redirect:/admin/surveys/record";
    }

    // 3. 문진 삭제 처리 (Soft Delete)
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        recordSurveyService.delete(id);
        return "redirect:/admin/surveys/record";
    }
}
