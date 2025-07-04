package com.example.demo.survey.controller;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.enums.TargetGroup;
import com.example.demo.survey.dto.SurveySetForm;
import com.example.demo.survey.dto.SurveySetSearchDto;
import com.example.demo.survey.entity.BaseSurvey;
import com.example.demo.survey.entity.SurveySet;
import com.example.demo.survey.service.SurveySetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/surveySet")
@RequiredArgsConstructor
public class SurveySetController {
    private final SurveySetService service;

    // 실제 선택 가능한 연령대만 (ALL, VARIOUS 제외)
    private final List<AgeGroup> ageOptions = Arrays.stream(AgeGroup.values())
            .filter(ag -> ag != AgeGroup.ALL && ag != AgeGroup.VARIOUS)
            .collect(Collectors.toList());

    // 실제 선택 가능한 카테고리만 (ALL, VARIOUS 제외)
    private final List<SurveyCategory> categoryOptions = Arrays.stream(SurveyCategory.values())
            .filter(sc -> sc != SurveyCategory.ALL && sc != SurveyCategory.VARIOUS)
            .collect(Collectors.toList());

    // TargetGroup 필터 옵션 (ALL 제외)
    private final List<TargetGroup> targetOptions = Arrays.stream(TargetGroup.values())
            .filter(t -> t != TargetGroup.ALL)
            .collect(Collectors.toList());

    // 관리자 리스트 + 필터
    @GetMapping
    public String list(
            @ModelAttribute("search") SurveySetSearchDto search,
            @PageableDefault(size = 10) Pageable pageable,
            Model model
    ) {
        Page<SurveySet> page = service.list(search, pageable);
        model.addAttribute("page", page);
        model.addAttribute("ageOptions", ageOptions);
        model.addAttribute("categoryOptions", categoryOptions);
        model.addAttribute("targetOptions",    targetOptions);
        return "admin/surveys/setList";
    }

    // 상세
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        SurveySet set = service.getDetail(id);
        model.addAttribute("set", set);
        List<? extends BaseSurvey> surveys = service.getSurveysBySetId(id);
        model.addAttribute("surveys", surveys);
        return "admin/surveys/setDetail";
    }

    // 생성 폼
    @GetMapping("/create")
    public String createForm(@RequestParam(defaultValue = "GROUP") String type, Model model) {
        SurveySetForm form = new SurveySetForm();
        form.setType(type);

        model.addAttribute("form", form);
        model.addAttribute("surveys",
                "SPECIAL".equals(type)
                        ? service.allSpecial(AgeGroup.ALL, SurveyCategory.ALL)
                        : service.allGroup(AgeGroup.ALL, SurveyCategory.ALL, TargetGroup.ALL)
        );
        model.addAttribute("targetOptions",
                Arrays.stream(TargetGroup.values())
                        .filter(t -> t != TargetGroup.ALL)
                        .collect(Collectors.toList())
        );
        model.addAttribute("ageOptions", ageOptions);
        model.addAttribute("categoryOptions", categoryOptions);
        return "admin/surveys/setForm";
    }

    // 수정 폼
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        SurveySet set = service.getDetail(id);
        SurveySetForm form = new SurveySetForm();
        form.setId(set.getSetId());
        form.setSetTitle(set.getSetTitle());
        form.setType(set.getType());
        form.setTargetGroup(set.getTargetGroup());

        // 이미 연결된 문진 ID
        List<? extends BaseSurvey> linked = service.getSurveysBySetId(id);
        form.setSurveyIds(linked.stream()
                .map(BaseSurvey::getId)
                .collect(Collectors.toList()));

        model.addAttribute("form", form);
        model.addAttribute("surveys",
                "SPECIAL".equals(set.getType())
                        ? service.allSpecial(AgeGroup.ALL, SurveyCategory.ALL)
                        : service.allGroup(AgeGroup.ALL, SurveyCategory.ALL, set.getTargetGroup())
        );
        model.addAttribute("targetOptions",
                Arrays.stream(TargetGroup.values())
                        .filter(t -> t != TargetGroup.ALL)
                        .collect(Collectors.toList())
        );
        model.addAttribute("ageOptions", ageOptions);
        model.addAttribute("categoryOptions", categoryOptions);
        return "admin/surveys/setForm";
    }

    // 저장 처리
    @PostMapping("/save")
    public String save(
            @Valid @ModelAttribute("form") SurveySetForm form,
            BindingResult br,
            Model model
    ) {
        // 바인딩 에러 시 다시 폼
        if (br.hasErrors()) {
            model.addAttribute("surveys",
                    "SPECIAL".equals(form.getType())
                            ? service.allSpecial(AgeGroup.ALL, SurveyCategory.ALL)
                            : service.allGroup(AgeGroup.ALL, SurveyCategory.ALL, form.getTargetGroup())
            );
            model.addAttribute("targetOptions",
                    Arrays.stream(TargetGroup.values())
                            .filter(t -> t != TargetGroup.ALL)
                            .collect(Collectors.toList())
            );
            model.addAttribute("ageOptions", ageOptions);
            model.addAttribute("categoryOptions", categoryOptions);
            return "admin/surveys/setForm";
        }

        // SPECIAL 타입인 경우 targetGroup 무시
        if ("SPECIAL".equals(form.getType())) {
            form.setTargetGroup(null);
        }

        try {
            service.createOrUpdate(form);
        } catch (IllegalArgumentException e) {
            br.reject(null, e.getMessage());
            model.addAttribute("surveys",
                    "SPECIAL".equals(form.getType())
                            ? service.allSpecial(AgeGroup.ALL, SurveyCategory.ALL)
                            : service.allGroup(AgeGroup.ALL, SurveyCategory.ALL, form.getTargetGroup())
            );
            model.addAttribute("targetOptions",
                    Arrays.stream(TargetGroup.values())
                            .filter(t -> t != TargetGroup.ALL)
                            .collect(Collectors.toList())
            );
            model.addAttribute("ageOptions", ageOptions);
            model.addAttribute("categoryOptions", categoryOptions);
            return "admin/surveys/setForm";
        }

        return "redirect:/admin/surveySet";
    }

    // 삭제 처리
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        service.deleteById(id);
        return "redirect:/admin/surveySet";
    }
}
