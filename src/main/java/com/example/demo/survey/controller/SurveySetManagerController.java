package com.example.demo.survey.controller;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.enums.TargetGroup;
import com.example.demo.survey.dto.SurveySetForm;
import com.example.demo.survey.dto.SurveySetSearchDto;
import com.example.demo.survey.entity.BaseSurvey;
import com.example.demo.survey.entity.SurveySet;
import com.example.demo.survey.service.SurveySetService;
import com.example.demo.users.entity.Manager;
import com.example.demo.users.repository.ManagerRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/manager/surveySet")
@RequiredArgsConstructor
public class SurveySetManagerController {

    private final SurveySetService service;
    private final ManagerRepository managerRepo;

    /** 1) LIST: 로그인 매니저의 targetGroup 고정 필터링 */
    @GetMapping
    public String list(
            @ModelAttribute("search") SurveySetSearchDto search,
            @PageableDefault(size = 10) Pageable pageable,
            Model model,
            Authentication auth
    ) {
        // 타입 고정
        search.setType("GROUP");

        // 로그인 매니저의 그룹
        Manager mgr = managerRepo.findByUsers_Uuid(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매니저"));
        TargetGroup myGroup = mgr.getGroup().getTargetGroup();

        // 검색 DTO 에 그룹만 세팅 (연령 필터 무시)
        search.setTargetGroup(myGroup);

        // 페이징 조회
        Page<SurveySet> page = service.list(search, pageable);

        model.addAttribute("page", page);
        // 연령 옵션 제거

        // 카테고리 옵션 추가
        model.addAttribute("categoryOptions", Arrays.asList(SurveyCategory.values()));

        return "manager/surveys/setList";
    }

    /** 2) DETAIL (변경 없음) */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        SurveySet set = service.getDetail(id);
        model.addAttribute("set", set);

        List<? extends BaseSurvey> surveys = service.getSurveysBySetId(id);
        model.addAttribute("surveys", surveys);

        return "manager/surveys/setDetail";
    }

    /** 3) CREATE FORM: 매니저 그룹의 문진만 로드 */
    @GetMapping("/create")
    public String createForm(Model model, Authentication auth) {
        // 로그인 매니저의 targetGroup
        TargetGroup myGroup = managerRepo.findByUsers_Uuid(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매니저"))
                .getGroup().getTargetGroup();

        SurveySetForm form = new SurveySetForm();
        form.setType("GROUP");
        form.setTargetGroup(myGroup);

        model.addAttribute("form", form);
        // myGroup 필터 적용된 문진만
        model.addAttribute("surveys",
                service.allGroup(AgeGroup.ALL, SurveyCategory.ALL, myGroup)
        );

        model.addAttribute("categoryOptions", Arrays.asList(SurveyCategory.values()));

        return "manager/surveys/setForm";
    }

    /** 4) EDIT FORM: 매니저 그룹의 문진만 로드 */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, Authentication auth) {
        // 로그인 매니저의 targetGroup
        TargetGroup myGroup = managerRepo.findByUsers_Uuid(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매니저"))
                .getGroup().getTargetGroup();

        SurveySet set = service.getDetail(id);
        SurveySetForm form = new SurveySetForm();
        form.setId(set.getSetId());
        form.setSetTitle(set.getSetTitle());
        form.setType("GROUP");
        form.setTargetGroup(myGroup);
        form.setSurveyIds(set.getGroupSurveys().stream()
                .map(BaseSurvey::getId).toList());

        model.addAttribute("form", form);
        // myGroup 필터 적용된 문진만
        model.addAttribute("surveys",
                service.allGroup(AgeGroup.ALL, SurveyCategory.ALL, myGroup)
        );

        model.addAttribute("categoryOptions", Arrays.asList(SurveyCategory.values()));

        return "manager/surveys/setForm";
    }

    /** 5) SAVE */
    @PostMapping("/save")
    public String save(
            @Valid @ModelAttribute("form") SurveySetForm form,
            BindingResult br,
            Model model
    ) {
        if (br.hasErrors()) {
            // 그룹 정보는 form에 이미 들어있으므로 그대로 사용
            model.addAttribute("surveys",
                    service.allGroup(AgeGroup.ALL, SurveyCategory.ALL, form.getTargetGroup())
            );
            return "manager/surveys/setForm";
        }
        // 엔티티 저장
        service.createOrUpdate(form);
        return "redirect:/manager/surveySet";
    }

    /** 6) DELETE */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        service.deleteById(id);
        return "redirect:/manager/surveySet";
    }
}
