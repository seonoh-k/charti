//package com.example.demo.fcm.controller;
//
//import com.example.demo.enums.AgeGroup;
//import com.example.demo.enums.FcmCategory;
//import com.example.demo.fcm.service.FcmService;
//import com.example.demo.repository.GroupRepository;
//import com.example.demo.users.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Controller;
//import com.example.demo.entity.Group;
//import com.example.demo.enums.TargetGroup;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Controller
//@RequestMapping("/admin/notification")
//@RequiredArgsConstructor
//public class FcmNotificationController {
//
//    private final FcmService fcmService;
//    private final UserRepository userRepo;
//    private final GroupRepository groupRepo;
//
//    // 알림 작성 폼 페이지
//    @GetMapping("/form")
//    public String showNotificationForm(Model model) {
//        model.addAttribute("categories", FcmCategory.values());
//        model.addAttribute("ageGroups", AgeGroup.values());
//
//        // ★ 그룹 리스트 (targetGroup: "유치원", "어린이집", "보육원" 등)
//        List<Group> groups = groupRepo.findAll().stream()
//                .filter(g -> g.getTargetGroup() != null)
//                .collect(Collectors.toList());
//        model.addAttribute("groups", groups);
//        return "admin/notification/notificationForm";
//    }
//
//    // 알림 발송 요청 처리
//    @PostMapping("/send")
//    public String sendNotification(
//            @RequestParam String target,
//            @RequestParam(required = false) String title,
//            @RequestParam(required = false) String body,
//            @RequestParam(required = false) FcmCategory category,
//            @RequestParam(required = false) AgeGroup ageGroup,
//            RedirectAttributes redirectAttributes) {
//
//        int successCount;
//        if ("SPECIAL_RISK".equals(target)) {
//            successCount = fcmService.sendNotificationToRiskGroupChildren();
//        }
//        else if (target.startsWith("GROUP_")) {
//            // ★ GROUP_ 뒤에 붙은 enum name 을 꺼내서 처리
//            String enumName = target.substring("GROUP_".length());
//            TargetGroup tg = TargetGroup.valueOf(enumName);
//            successCount = fcmService.sendNotificationToGroupChildren(tg);
//        }
//        else {
//            successCount = fcmService.sendNotificationToTarget(
//                    title, body, category, ageGroup);
//        }
//
//        redirectAttributes.addFlashAttribute(
//                "message", "알림 발송 완료 (" + successCount + "명 수신)");
//        return "redirect:/admin/notification/form";
//    }
//}

package com.example.demo.fcm.controller;

import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.FcmCategory;
import com.example.demo.enums.TargetGroup;
import com.example.demo.fcm.dto.AdminFcmSendResultDto;
import com.example.demo.fcm.service.FcmService;
import com.example.demo.repository.GroupRepository;
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
    private final GroupRepository groupRepo;

    @GetMapping("/form")
    public String showNotificationForm(Model model) {
        model.addAttribute("categories", FcmCategory.values());
        model.addAttribute("ageGroups", AgeGroup.values());

        // TODO: groupRepo에 findDistinctTargetGroups() 같은 메소드를 만들어 중복을 제거하는 것이 좋습니다.
        model.addAttribute("groups", groupRepo.findAll());
        return "admin/notification/notificationForm";
    }

    @PostMapping("/send")
    public String sendNotification(
            @RequestParam String target,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String body,
            @RequestParam(required = false) FcmCategory category,
            @RequestParam(required = false) AgeGroup ageGroup,
            RedirectAttributes redirectAttributes) {

        AdminFcmSendResultDto result;

        if ("SPECIAL_RISK".equals(target)) {
            result = fcmService.sendNotificationToRiskGroupChildren();
        } else if (target.startsWith("GROUP_")) {
            String enumName = target.substring("GROUP_".length());
            TargetGroup tg = TargetGroup.valueOf(enumName);
            result = fcmService.sendNotificationToGroupChildren(tg);
        } else {
            result = fcmService.sendNotificationToTarget(title, body, category, ageGroup);
        }

        // 결과를 Flash 속성으로 추가하여 리다이렉트 후에도 데이터가 유지되도록 함
        redirectAttributes.addFlashAttribute("sendResult", result);
        redirectAttributes.addFlashAttribute("message", "✅ 알림이 정상적으로 요청되었습니다. 아래에서 발송 결과를 확인하세요.");

        return "redirect:/admin/notification/form";
    }
}