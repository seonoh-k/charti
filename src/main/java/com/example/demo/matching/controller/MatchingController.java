package com.example.demo.matching.controller;

import com.example.demo.enums.MatchingStatus;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.matching.dto.MatchingRequestDto;
import com.example.demo.matching.entity.Matching;
import com.example.demo.matching.entity.MatchingAnswer;
import com.example.demo.matching.service.MatchingAnswerService;
import com.example.demo.matching.service.MatchingService;
import com.example.demo.survey.entity.SpecialAnswer;
import com.example.demo.survey.repository.SpecialAnswerRepository;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Expert;
import com.example.demo.users.repository.ChildRepository;
import com.example.demo.users.repository.ExpertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;
    private final ChildRepository childRepository;
    private final SpecialAnswerRepository specialAnswerRepository;
    private final ExpertRepository expertRepository;
    private final MatchingAnswerService answerService;

    /** 1) SurveyCategory별 상담 신청 폼 */
    @GetMapping("/{category}/request")
    public String showRequestForm(
            @PathVariable SurveyCategory category,
            @RequestParam("childId") Long childId,
            @RequestParam("answerId") List<Long> answerIds,
            Model model) {

        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 자녀 ID입니다."));
        List<SpecialAnswer> answers = specialAnswerRepository.findAllById(answerIds);
        if (answers.isEmpty()) throw new IllegalArgumentException("문의 대상이 비어있습니다.");

        MatchingRequestDto form = new MatchingRequestDto();
        form.setChildId(childId);
        form.setCategory(category);
        form.setAnswerIds(answerIds);

        model.addAttribute("child", child);
        model.addAttribute("answers", answers);
        model.addAttribute("matchingRequest", form);

        return "matching/request";
    }

    /** 2) 상담 신청 처리 */
    @PostMapping
    public String submitRequest(@ModelAttribute MatchingRequestDto req) {
        if (req.getChildId() == null || req.getAnswerIds().isEmpty())
            throw new IllegalArgumentException("자녀 또는 문의 대상이 비어있습니다.");

        Child child = childRepository.findById(req.getChildId())
                .orElseThrow(() -> new IllegalArgumentException("잘못된 자녀 정보입니다."));
        List<SpecialAnswer> answers = specialAnswerRepository.findAllById(req.getAnswerIds());
        if (answers.isEmpty()) throw new IllegalArgumentException("문의 대상 정보가 비어있습니다.");

        Matching m = new Matching();
        m.setChild(child);
        m.setCategory(req.getCategory());
        m.setTitle(req.getTitle());
        m.setContent(req.getContent());
        answers.forEach(a -> a.setMatching(m));
        m.getAnswers().addAll(answers);
        matchingService.save(m);

        return "redirect:/";
    }

    /** 3) 부모용 상세보기 (상담 & 답변 & —REQUESTED— 시에만 전문가 배정용 list 전달) */
    @GetMapping("/detail/{id}")
    public String parentDetail(
            @PathVariable Long id,
            Principal principal,
            Model model
    ) {
        Matching m = matchingService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 상담 ID입니다."));
        List<MatchingAnswer> answers = answerService.findByMatchingId(id);

        model.addAttribute("matching", m);
        model.addAttribute("answers", answers);

        if (m.getStatus() == MatchingStatus.REQUESTED) {
            List<Expert> experts = expertRepository
                    .findAllByMajor(m.getCategory().getDisplayName());
            model.addAttribute("experts", experts);
        }

        return "matching/detail";
    }

}
