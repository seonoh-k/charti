package com.example.demo.fcm.controller;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.FcmCategory;
import com.example.demo.fcm.service.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/notification")
@RequiredArgsConstructor
public class FcmNotificationController {

    private final FcmService fcmService;

    // 알림 작성 폼 페이지
    @GetMapping("/form")
    public String showNotificationForm(Model model) {
        model.addAttribute("categories", FcmCategory.values());
        model.addAttribute("ageGroups", AgeGroup.values());
        return "admin/notification/notificationForm";
    }

    // 알림 발송 요청 처리
    @PostMapping("/send")
    public String sendNotification(@RequestParam String title,
                                   @RequestParam String body,
                                   @RequestParam(required = false) FcmCategory category,
                                   @RequestParam(required = false) AgeGroup ageGroup,
                                   RedirectAttributes redirectAttributes) {

        int successCount = fcmService.sendNotificationToTarget(title, body, category, ageGroup);
        redirectAttributes.addFlashAttribute("message", "알림 발송 완료 (" + successCount + "명 수신)");
        return "redirect:/admin/notification/form";
    }
}
