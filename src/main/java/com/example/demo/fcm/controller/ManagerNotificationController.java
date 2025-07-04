package com.example.demo.fcm.controller;


import com.example.demo.dto.UserDTO;
import com.example.demo.fcm.dto.AdminFcmSendResultDto;
import com.example.demo.fcm.dto.FcmSendResultDto;
import com.example.demo.fcm.service.FcmService;
import com.example.demo.survey.entity.SurveySet;
import com.example.demo.survey.service.SurveySetService;
import com.example.demo.users.service.AuthService;
import com.example.demo.users.service.ManagerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/manager/notification")
@RequiredArgsConstructor
@Slf4j
public class ManagerNotificationController {

    private final AuthService authService;
    private final SurveySetService surveySetService;
    private final FcmService fcmService;

    /**
     * 문진 알림 발송 폼
     */
    @GetMapping("/form")
    public String showNotificationForm(Model model) {
        UserDTO user = authService.getLoginUser();
        Long managerId = user.getId();

        List<SurveySet> surveySets = surveySetService.getSetsForManager(managerId);

        log.info("🔎 매니저 ID: {}, 조회된 문진 세트 수: {}", managerId, surveySets.size());
        for (SurveySet set : surveySets) {
            log.info("📌 세트 ID: {}, 제목: {}, 타입: {}, 그룹: {}", set.getSetId(), set.getSetTitle(), set.getType(), set.getTargetGroup());
        }

        model.addAttribute("surveySets", surveySets);
        return "manager/notification/notificationForm";
    }

    /**
     * 문진 알림 발송 처리
     */
    @PostMapping("/send")
    public String sendNotification(@RequestParam("setId") Long setId, Model model) {
        UserDTO user = authService.getLoginUser();
        Long managerId = user.getId();

        try {
            // 서비스로부터 발송 결과(DTO)를 받습니다.
            AdminFcmSendResultDto result = fcmService.sendSurveySetToGroupMembers(managerId, setId);
            model.addAttribute("sendResult", result);
            model.addAttribute("message", "✅ 문진 알림이 정상적으로 요청되었습니다. 아래에서 발송 결과를 확인하세요.");
        } catch (Exception e) {
            log.error("문진 알림 발송 실패", e);
            model.addAttribute("message", "❌ 문진 알림 발송 중 오류가 발생했습니다: " + e.getMessage());
        }

        List<SurveySet> surveySets = surveySetService.getSetsForManager(managerId);
        model.addAttribute("surveySets", surveySets);

        return "manager/notification/notificationForm";
    }
}
