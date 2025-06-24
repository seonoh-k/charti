package com.example.demo.matching;

import com.example.demo.matching.dto.MatchingRequestDto;
import com.example.demo.matching.entity.Matching;
import com.example.demo.matching.service.MatchingService;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.survey.entity.SpecialAnswer;
import com.example.demo.survey.repository.SpecialAnswerRepository;
import com.example.demo.users.entity.Child;
import com.example.demo.users.repository.ChildRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/matching")
public class MatchingController {

    private final MatchingService matchingService;
    private final ChildRepository childRepository;
    private final SpecialAnswerRepository specialAnswerRepository;

    public MatchingController(MatchingService matchingService,
                              ChildRepository childRepository,
                              SpecialAnswerRepository specialAnswerRepository) {
        this.matchingService = matchingService;
        this.childRepository = childRepository;
        this.specialAnswerRepository = specialAnswerRepository;
    }

    // 1) SurveyCategory별 상담 신청 페이지
    @GetMapping("/{category}/request")
    public String showRequestForm(@PathVariable SurveyCategory category,
                                  @RequestParam("childId") Long childId,
                                  @RequestParam("answerId") List<Long> answerIds,
                                  Model model) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 자녀 ID입니다."));
        List<SpecialAnswer> answers = specialAnswerRepository.findAllById(answerIds)
                .stream().toList();
        if (answers.isEmpty()) {
            throw new IllegalArgumentException("잘못된 문의 대상 ID들입니다.");
        }

        model.addAttribute("child", child);
        model.addAttribute("answer", answers);
        model.addAttribute("category", category);
        model.addAttribute("matchingRequest", new MatchingRequestDto());
        return "matching/request";  // templates/matching/request.html
    }

    // 2) 상담 신청 처리
    @PostMapping
    public String submitRequest(@ModelAttribute MatchingRequestDto req) {
        // --- 1) 요청 데이터 유효성 검증 ---
        if (req.getChildId() == null || req.getAnswerIds() == null || req.getAnswerIds().isEmpty()) {
            throw new IllegalArgumentException("자녀 또는 문의 대상이 비어있습니다.");
        }

        // --- 2) 자녀 조회 ---
        Child child = childRepository.findById(req.getChildId())
                .orElseThrow(() -> new IllegalArgumentException("잘못된 자녀 정보입니다."));

        // --- 3) SpecialAnswer 복수 조회 ---
        List<SpecialAnswer> answers = specialAnswerRepository.findAllById(req.getAnswerIds());
        if (answers.isEmpty()) {
            throw new IllegalArgumentException("잘못된 문의 대상 정보입니다.");
        }

        // --- 4) 각각의 답변마다 Matching 생성 & 저장 ---
        for (SpecialAnswer answer : answers) {
            Matching m = new Matching();
            m.setChild(child);
//            m.setSpecialAnswer(answer);
            m.setCategory(req.getCategory());
            m.setTitle(req.getTitle());
            m.setContent(req.getContent());
            // status, createdAt 등은 BaseEntity에서 자동 세팅
            matchingService.save(m);
        }

        // --- 5) 완료 후 리다이렉트 ---
        return "redirect:/";
    }

}
