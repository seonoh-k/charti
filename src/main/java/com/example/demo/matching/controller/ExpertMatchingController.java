package com.example.demo.matching.controller;

import com.example.demo.matching.entity.Matching;
import com.example.demo.matching.entity.MatchingAnswer;
import com.example.demo.matching.service.MatchingAnswerService;
import com.example.demo.matching.service.MatchingService;
import com.example.demo.users.entity.Expert;
import com.example.demo.users.repository.ExpertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/expert/matching")
@RequiredArgsConstructor
public class ExpertMatchingController {

    private final MatchingService matchingService;
    private final ExpertRepository expertRepo;
    private final MatchingAnswerService answerService;

    /** 상세보기 + 답변 폼 */
    @GetMapping("/{matchId}")
    public String detail(
            @PathVariable Long matchId,
            Principal principal,
            Model model
    ) {
        // --- 1) 상담 & 전문가 검증 ---
        Matching m = matchingService.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 상담 ID"));
        Expert me = expertRepo.findByUsersUuid(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("전문가 정보가 없습니다."));
        if (m.getExpert() == null || !m.getExpert().getId().equals(me.getId())) {
            throw new IllegalStateException("본인에게 배정되지 않은 상담입니다.");
        }

        // --- 2) 모델에 담기 ---
        model.addAttribute("matching", m);
        model.addAttribute("answerForm", new MatchingAnswer());

        // **전문가가 남긴 답변 리스트도** 반드시 넘겨줘야 thymeleaf 에서 출력됩니다.
        List<MatchingAnswer> answers = answerService.findByMatchingId(matchId);
        model.addAttribute("answers", answers);

        return "expert/matchingDetail";
    }

    /** 답변 저장 */
    @PostMapping("/{matchId}/respond")
    public String respond(
            @PathVariable Long matchId,
            @ModelAttribute("answerForm") MatchingAnswer form,
            Principal principal
    ) {
        Matching m = matchingService.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 상담 ID"));
        Expert me = expertRepo.findByUsersUuid(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("전문가 정보가 없습니다."));

        // 답변 엔티티 세팅
        form.setMatching(m);
        form.setExpert(me);
        form.setChildId(m.getChild().getId());
        answerService.save(form);

        return "redirect:/expert/matching/" + matchId + "?submitted";
    }
}
