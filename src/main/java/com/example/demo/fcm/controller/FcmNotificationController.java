//package com.example.demo.fcm.controller;
//
//import com.example.demo.dto.AdminDTO;
//import com.example.demo.enums.AgeGroup;
//import com.example.demo.enums.FcmCategory;
//import com.example.demo.enums.TargetGroup;
//import com.example.demo.fcm.dto.AdminFcmSendResultDto;
//import com.example.demo.fcm.service.FcmService;
//import com.example.demo.repository.GroupRepository;
//import com.example.demo.users.entity.Admin;
//import com.example.demo.users.entity.Users;
//import com.example.demo.users.repository.UserRepository;
//import com.example.demo.users.service.AdminService;
//import com.example.demo.users.service.AuthService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//import org.springframework.security.core.Authentication;
//
//import java.util.List;
//
///**
// * 관리자용 FCM 알림 발송 컨트롤러
// */
//@Controller
//@RequestMapping("/admin/notification")
//@RequiredArgsConstructor
//public class FcmNotificationController {
//
//    private final FcmService fcmService;
//    private final GroupRepository groupRepo;
//    private final UserRepository userRepository;
//    private final AuthService authService;
//    private final AdminService adminService;
//
//    /**
//     * 알림 작성 폼 페이지를 보여줍니다.
//     * @param model 뷰 렌더링을 위한 모델
//     * @return 알림 작성 페이지 경로
//     */
//    @GetMapping("/form")
//    public String showNotificationForm(Model model) {
//        model.addAttribute("categories", FcmCategory.values());
//        model.addAttribute("ageGroups", AgeGroup.values());
//        List<TargetGroup> targetGroups = groupRepo.findDistinctTargetGroups();
//        model.addAttribute("targetGroups", targetGroups);
//        return "admin/notification/notificationForm";
//    }
//
//    /**
//     * 알림 발송 요청을 처리하고 결과를 리다이렉트로 전달합니다.
//     *
//     * @param target 발송 대상 타입 (e.g., "ALL", "SPECIAL_RISK", "GROUP_유치원")
//     * @param title 알림 제목
//     * @param body 알림 내용
//     * @param category 알림 카테고리
//     * @param ageGroup 연령대 필터 (선택적)
//     * @param authentication 로그인 관리자 정보
//     * @param redirectAttributes 리다이렉트 시 메시지 전달용 속성
//     * @return 알림 작성 폼으로 리다이렉트
//     */
//    @PostMapping("/send")
//    public String sendNotification(
//            @RequestParam String target,
//            @RequestParam(required = false) String title,
//            @RequestParam(required = false) String body,
//            @RequestParam(required = false) FcmCategory category,
//            @RequestParam(required = false) AgeGroup ageGroup,
//            Authentication authentication,
//            RedirectAttributes redirectAttributes) {
//
//        AdminDTO adminDTO = authService.getLoginAdmin();
//        Admin admin = adminService.getAdminById(adminDTO.getId());
//
//        AdminFcmSendResultDto result;
//
//        if ("SPECIAL_RISK".equals(target)) {
//            result = fcmService.sendNotificationToRiskGroupChildren(admin);
//        } else if (target.startsWith("GROUP_")) {
//            String enumName = target.substring("GROUP_".length());
//            TargetGroup tg = TargetGroup.valueOf(enumName);
//            result = fcmService.sendNotificationToGroupChildren(admin, tg);
//        } else {
//            result = fcmService.sendNotificationToTarget(admin, title, body, category, ageGroup);
//        }
//
//        redirectAttributes.addFlashAttribute("sendResult", result);
//        redirectAttributes.addFlashAttribute("message", "✅ 알림이 정상적으로 요청되었습니다. 아래에서 발송 결과를 확인하세요.");
//
//        return "redirect:/admin/notification/form";
//    }
//}
package com.example.demo.fcm.controller;

import com.example.demo.dto.AdminDTO;
import com.example.demo.enums.AgeGroup;
import com.example.demo.enums.FcmCategory;
import com.example.demo.enums.SurveyCategory;
import com.example.demo.enums.TargetGroup;
import com.example.demo.fcm.dto.AdminFcmSendResultDto;
import com.example.demo.fcm.service.FcmService;
import com.example.demo.survey.entity.SurveySet;
import com.example.demo.survey.service.SurveySetService;
import com.example.demo.users.entity.Admin;
import com.example.demo.users.service.AdminService;
import com.example.demo.users.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 관리자용 FCM 알림 발송 컨트롤러
 */
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Controller
@RequestMapping("/admin/notification")
@RequiredArgsConstructor
public class FcmNotificationController {

    private final FcmService fcmService;
    private final AuthService authService;
    private final AdminService adminService;
    private final SurveySetService surveySetService;

    /**
     * 알림 작성 폼 페이지를 보여줍니다.
     * - 필터링에 필요한 모든 Enum 목록과 문진 세트 목록을 조회하여 View에 전달합니다.
     */
    @GetMapping("/form")
    public String showNotificationForm(Model model) {
        // 1. 알림 자체의 카테고리 목록
        model.addAttribute("fcmCategories", FcmCategory.values());

        // 2. 위험군 필터링 조건용 설문 카테고리 목록
        List<SurveyCategory> filteredSurveyCategories = Arrays.stream(SurveyCategory.values())
                .filter(sc -> sc != SurveyCategory.ALL && sc != SurveyCategory.VARIOUS)
                .collect(Collectors.toList());
        model.addAttribute("surveyCategories", filteredSurveyCategories);

        // 3. 연령대 필터 목록 (ALL, VARIOUS 제외)
        List<AgeGroup> filteredAgeGroups = Arrays.stream(AgeGroup.values())
                .filter(a -> a != AgeGroup.ALL && a != AgeGroup.VARIOUS)
                .collect(Collectors.toList());
        model.addAttribute("ageGroups", filteredAgeGroups);

        // 4. 기관 그룹 목록
        List<TargetGroup> allTargetGroups = Arrays.asList(TargetGroup.values());
        List<TargetGroup> filteredTargetGroups = allTargetGroups.stream()
                .filter(tg -> tg != TargetGroup.ALL)
                .collect(Collectors.toList());
        model.addAttribute("targetGroups", filteredTargetGroups);

        return "admin/notification/notificationForm";
    }

    /**
     * 알림 발송 요청을 처리하고 결과를 리다이렉트로 전달합니다.
     * - FcmService의 메인 메소드를 호출합니다.
     */
    @PostMapping("/send")
    public String sendNotification(
            @RequestParam String target, // "ALL", "SPECIAL_RISK", "GROUP_KINDERGARTEN" 등
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String body,
            @RequestParam(required = false) AgeGroup ageGroup,
            @RequestParam(required = false) FcmCategory fcmCategory,
            @RequestParam(required = false) SurveyCategory surveyCategory,
            @RequestParam(required = false) Long setId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        AdminDTO adminDTO = authService.getLoginAdmin();
        Admin admin = adminService.getAdminById(adminDTO.getId());

        String targetType = target;
        TargetGroup targetGroup = null;

        if (target.startsWith("GROUP_")) {
            targetType = "GROUP";
            try {
                targetGroup = TargetGroup.valueOf(target.substring("GROUP_".length()));
            } catch (IllegalArgumentException e) {
                // 잘못된 그룹 값 처리
                redirectAttributes.addFlashAttribute("message", "❌ 유효하지 않은 그룹 대상입니다.");
                return "redirect:/admin/notification/form";
            }
        }

        // 새로운 통합 서비스 메소드 호출
        AdminFcmSendResultDto result = fcmService.sendAdvancedNotification(
                admin, targetType, ageGroup, targetGroup, fcmCategory, surveyCategory, setId, title, body
        );
        log.info("▶ 발송 결과 DTO: {}", result);
        if (result.getTotalTargetCount() == 0) {
            redirectAttributes.addFlashAttribute("message", "ℹ️ 해당 조건에 맞는 발송 대상자가 없습니다.");
        } else {
            redirectAttributes.addFlashAttribute("sendResult", result);
            redirectAttributes.addFlashAttribute("message", "✅ 알림이 정상적으로 요청되었습니다. 아래에서 발송 결과를 확인하세요.");
        }
        return "redirect:/admin/notification/form";
    }
}