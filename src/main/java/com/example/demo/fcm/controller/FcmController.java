package com.example.demo.fcm.controller;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.FcmCategory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FcmController {

    // 알림 발송 폼 GET
//    @GetMapping("/admin/notification/send")
//    public String showNotificationForm(Model model) {
//        model.addAttribute("ageGroups", AgeGroup.values());
//        model.addAttribute("categories", FcmCategory.values());
//        return "admin/notification/notificationForm";
//    }

    // 알림 발송 POST는 이후 구현
}
