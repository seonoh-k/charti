package com.example.demo.fcm.controller;

import com.example.demo.enums.FcmCategory;
import com.example.demo.fcm.dto.FcmHistorySearchDto;
import com.example.demo.fcm.entity.FcmSendHistory;
import com.example.demo.fcm.service.FcmHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 관리자용 FCM 알림 발송 이력 페이지 컨트롤러
 * <p>
 * /admin/notification/history 경로로 접근하며,
 * 검색 조건과 페이징을 기반으로 FCM 발송 이력을 조회하고,
 * 뷰 템플릿에 데이터를 전달합니다.
 * </p>
 */
@Controller
@RequestMapping("/admin/notification/history")
@RequiredArgsConstructor
public class AdminFcmHistoryController {

    private final FcmHistoryService historyService;

    /**
     * FCM 발송 이력 페이지 진입 및 검색 결과 출력
     *
     * @param searchDto 검색 조건 (발송일, 카테고리, 발신자 등)
     * @param pageable  페이지네이션 정보 (기본 정렬: sentAt 기준 내림차순)
     * @param model     Thymeleaf 렌더링을 위한 모델 객체
     * @return 알림 발송 이력 페이지 템플릿 경로
     */
    @GetMapping
    public String historyPage(
            @ModelAttribute("search") FcmHistorySearchDto searchDto,
            @PageableDefault(sort = "sentAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model
    ) {
        Page<FcmSendHistory> historyPage = historyService.findHistory(searchDto, pageable);

        model.addAttribute("historyPage", historyPage);            // 페이징된 이력 목록
        model.addAttribute("categories", FcmCategory.values());    // 카테고리 선택 필터용

        return "admin/notification/notificationView";
    }
}
