package com.example.demo.fcm.controller;


import com.example.demo.dto.UserDTO;
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
    private final ManagerService managerService;

    /**
     * 문진 알림 발송 폼
     */
    @GetMapping("/form")
    public String showNotificationForm(Model model) {
        UserDTO user = authService.getLoginUser();
        Long managerId = user.getId();

        List<SurveySet> surveySets = surveySetService.getSetsForManager(managerId); // 담당자에게 배정된 세트만 조회
        model.addAttribute("surveySets", surveySets);
        return "manager/notification/notificationForm";
    }

    /**
     * 문진 알림 발송 처리
     */
    @PostMapping("/send")
    public String sendNotification(@RequestParam("setId") Long setId,
                                   Model model,
                                   HttpServletRequest request) {
        UserDTO user = authService.getLoginUser();

        try {
            fcmService.sendSurveySetToGroupMembers(user.getId(), setId); // 해당 담당자의 그룹 구성원에게 전송
            model.addAttribute("message", "✅ 문진 알림이 성공적으로 발송되었습니다.");
        } catch (Exception e) {
            log.error("문진 알림 발송 실패", e);
            model.addAttribute("message", "❌ 문진 알림 발송 중 오류가 발생했습니다.");
        }

        return "manager/notification/notificationForm";
    }
}
