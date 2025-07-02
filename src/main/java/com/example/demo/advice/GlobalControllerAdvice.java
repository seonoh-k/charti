package com.example.demo.advice;

import com.example.demo.enums.FcmCategory;
import com.example.demo.fcm.repository.NoticeRepository;
import com.example.demo.users.repository.UserRepository;
import com.example.demo.users.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {
    private final NoticeRepository noticeRepo;
    private final UserRepository   userRepo;

    @ModelAttribute("unreadCount")
    public long unreadCount(Principal principal) {
        if (principal == null) return 0;
        Users me = userRepo.findByUuid(principal.getName()).orElse(null);
        if (me == null) return 0;
        return noticeRepo.countByUserAndDeletedFalseAndReadFlagFalse(me);
    }
    /**
     * [추가] 모든 뷰에 FCM 카테고리 목록을 전달하는 메소드
     */
    @ModelAttribute("fcmCategories")
    public List<FcmCategory> fcmCategories() {
        // 모든 FcmCategory 값을 리스트로 반환합니다.
        // 만약 특정 카테고리를 제외하고 싶다면 .filter() 등을 추가할 수 있습니다.
        return Arrays.stream(FcmCategory.values()).collect(Collectors.toList());
    }
}
