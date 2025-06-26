package com.example.demo.fcm.controller;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.FcmCategory;
import com.example.demo.enums.TargetGroup;
import com.example.demo.fcm.dto.AdminFcmSendResultDto;
import com.example.demo.fcm.service.FcmService;
import com.example.demo.repository.GroupRepository;
import com.example.demo.users.entity.Users;
import com.example.demo.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

import java.util.List;

/**
 * 관리자용 FCM 알림 발송 컨트롤러
 */
@Controller
@RequestMapping("/admin/notification")
@RequiredArgsConstructor
public class FcmNotificationController {

    private final FcmService fcmService;
    private final GroupRepository groupRepo;
    private final UserRepository userRepository;

    /**
     * 알림 작성 폼 페이지를 보여줍니다.
     * @param model 뷰 렌더링을 위한 모델
     * @return 알림 작성 페이지 경로
     */
    @GetMapping("/form")
    public String showNotificationForm(Model model) {
        model.addAttribute("categories", FcmCategory.values());
        model.addAttribute("ageGroups", AgeGroup.values());

        List<TargetGroup> targetGroups = groupRepo.findDistinctTargetGroups();
        model.addAttribute("targetGroups", targetGroups);
        return "admin/notification/notificationForm";
    }

    /**
     * 알림 발송 요청을 처리하고 결과를 리다이렉트로 전달합니다.
     *
     * @param target 발송 대상 타입 (e.g., "ALL", "SPECIAL_RISK", "GROUP_유치원")
     * @param title 알림 제목
     * @param body 알림 내용
     * @param category 알림 카테고리
     * @param ageGroup 연령대 필터 (선택적)
     * @param authentication 로그인 관리자 정보
     * @param redirectAttributes 리다이렉트 시 메시지 전달용 속성
     * @return 알림 작성 폼으로 리다이렉트
     */
    @PostMapping("/send")
    public String sendNotification(
            @RequestParam String target,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String body,
            @RequestParam(required = false) FcmCategory category,
            @RequestParam(required = false) AgeGroup ageGroup,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        Users adminUser = userRepository.findByUuid(authentication.getName())
                .orElseThrow(() -> new RuntimeException("관리자 정보를 찾을 수 없습니다."));

        AdminFcmSendResultDto result;

        if ("SPECIAL_RISK".equals(target)) {
            result = fcmService.sendNotificationToRiskGroupChildren(adminUser);
        } else if (target.startsWith("GROUP_")) {
            String enumName = target.substring("GROUP_".length());
            TargetGroup tg = TargetGroup.valueOf(enumName);
            result = fcmService.sendNotificationToGroupChildren(adminUser, tg);
        } else {
            result = fcmService.sendNotificationToTarget(adminUser, title, body, category, ageGroup);
        }

        redirectAttributes.addFlashAttribute("sendResult", result);
        redirectAttributes.addFlashAttribute("message", "✅ 알림이 정상적으로 요청되었습니다. 아래에서 발송 결과를 확인하세요.");

        return "redirect:/admin/notification/form";
    }
}
