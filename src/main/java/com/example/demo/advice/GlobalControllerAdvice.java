package com.example.demo.advice;

import com.example.demo.fcm.repository.NoticeRepository;
import com.example.demo.users.repository.UserRepository;
import com.example.demo.users.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

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
}
