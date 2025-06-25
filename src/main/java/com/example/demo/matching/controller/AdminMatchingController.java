package com.example.demo.matching.controller;

import com.example.demo.enums.FcmCategory;
import com.example.demo.enums.MatchingStatus;
import com.example.demo.fcm.service.FcmService;
import com.example.demo.matching.entity.Matching;
import com.example.demo.users.entity.Expert;
import com.example.demo.users.entity.Users;
import com.example.demo.users.repository.ExpertRepository;
import com.example.demo.matching.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/matching")
@RequiredArgsConstructor
public class AdminMatchingController {

    private final MatchingService matchingService;
    private final ExpertRepository expertRepository;
    private final FcmService fcmService;

    /**
     * 0) 관리자용 목록 조회 (기본 ALL → 전체)
     */
    @GetMapping
    public String listByStatus(
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        PageRequest pr = PageRequest.of(page, 10, Sort.by("createdAt").descending());
        Page<Matching> matchings;
        if ("ALL".equals(status)) {
            matchings = matchingService.findAll(pr);
        } else {
            MatchingStatus ms = MatchingStatus.valueOf(status);
            matchings = matchingService.findByStatus(ms, pr);
        }

        List<String> statuses = List.of("ALL", "REQUESTED", "MATCHED", "RESPONDED");

        model.addAttribute("matchings", matchings);
        model.addAttribute("statuses", statuses);
        model.addAttribute("currentStatus", status);
        return "admin/matching/list";
    }


    /**
     * 3) 전문가 배정 처리 및 알림 발송
     */
    @PostMapping("/{id}/assign")
    public String assignExpert(
            @PathVariable Long id,
            @RequestParam Long expertId
    ) {
        Matching m = matchingService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 상담 ID입니다."));
        if (m.getStatus() != MatchingStatus.REQUESTED) {
            throw new IllegalStateException("신청완료된 상담만 배정할 수 있습니다.");
        }

        Expert expert = expertRepository.findById(expertId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 전문가 ID입니다."));

        // 1) 엔티티 연결 및 상태 변경
        m.setExpert(expert);
        m.setStatus(MatchingStatus.MATCHED);
        matchingService.save(m);

        // 2) FCM 알림
        Users expertUser = expert.getUsers();
        String title = "새 상담이 배정되었습니다";
        String body  = String.format(
                "[%s] %s 자녀(%s세) 상담 — \"%s\"",
                m.getCategory().getDisplayName(),
                m.getChild().getName(),
                m.getChild().getAge(),
                m.getTitle()
        );
        String url   = "/expert/matching/" + m.getId();
        fcmService.sendNotificationToUser(
                expertUser, title, body,
                FcmCategory.SPECIAL,
                url
        );

        return "redirect:/admin/matching?status=" + MatchingStatus.MATCHED;
    }

}
