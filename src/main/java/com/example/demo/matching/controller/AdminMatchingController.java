package com.example.demo.matching.controller;

import com.example.demo.enums.MatchingStatus;
import com.example.demo.fcm.service.FcmService;
import com.example.demo.matching.entity.Matching;
import com.example.demo.matching.entity.MatchingAnswer;
import com.example.demo.matching.service.MatchingAnswerService;
import com.example.demo.matching.service.MatchingService;
import com.example.demo.users.entity.Expert;
import com.example.demo.users.entity.Users;
import com.example.demo.users.repository.ExpertRepository;
import com.example.demo.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/matching")
@RequiredArgsConstructor
public class AdminMatchingController {

    private final MatchingService       matchingService;
    private final MatchingAnswerService answerService;
    private final ExpertRepository      expertRepository;
    private final FcmService            fcmService;
    private final UserRepository        userRepository;

    /** 관리자용 목록 조회 */
    @GetMapping
    public String listByStatus(
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        PageRequest pr = PageRequest.of(page, 10, Sort.by("createdAt").descending());
        Page<Matching> matchings = "ALL".equals(status)
                ? matchingService.findAll(pr)
                : matchingService.findByStatus(MatchingStatus.valueOf(status), pr);

        model.addAttribute("matchings", matchings);
        model.addAttribute("statuses", List.of("ALL","REQUESTED","MATCHED","RESPONDED"));
        model.addAttribute("currentStatus", status);
        return "admin/matching/list";
    }

    /** 관리자용 상세보기 */
    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            Model model
    ) {
        Matching m = matchingService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 상담 ID입니다."));
        model.addAttribute("matching", m);

        // 1) 사진
        List<String> photos = Optional.ofNullable(m.getFilename())
                .filter(f -> !f.isBlank())
                .map(f -> List.of(f.split("\\s*,\\s*")))
                .orElse(List.of());
        model.addAttribute("photos", photos);

        // 2) **displayName** 으로 전문가 필터링
        String requiredMajor = m.getCategory().getDisplayName();
        List<Expert> experts = expertRepository.findByMajor(requiredMajor);
        model.addAttribute("experts", experts);

        // 3) 전문가 답변
        List<MatchingAnswer> expertAnswers = answerService.findByMatchingId(id);
        model.addAttribute("expertAnswers", expertAnswers);

        return "admin/matching/detail";
    }

    /** 전문가 배정 처리 */
    @PostMapping("/{id}/assign")
    public String assignExpert(
            @PathVariable Long id,
            @RequestParam Long expertId,
            Authentication authentication
    ) {
        Matching m = matchingService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 상담 ID입니다."));
        if (m.getStatus() != MatchingStatus.REQUESTED) {
            throw new IllegalStateException("신청완료된 상담만 배정할 수 있습니다.");
        }

        Expert expert = expertRepository.findById(expertId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 전문가 ID입니다."));
        m.setExpert(expert);
        m.setStatus(MatchingStatus.MATCHED);
        matchingService.save(m);

        Users adminUser = userRepository.findByUuid(authentication.getName())
                            .orElseThrow(() -> new RuntimeException("관리자 정보를 찾을 수 없습니다."));

        fcmService.sendNotification(
                adminUser,
                expert.getUsers(),
                "새 상담이 배정되었습니다",
                String.format("[%s] %s 자녀(%s세) 상담 — \"%s\"",
                        m.getCategory().getDisplayName(),
                        m.getChild().getName(),
                        m.getChild().getAge(),
                        m.getTitle()),
                com.example.demo.enums.FcmCategory.SPECIAL,
                "/expert/matching/" + m.getId()
        );

        return "redirect:/admin/matching?status=" + MatchingStatus.MATCHED;
    }
}
