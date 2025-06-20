package com.example.demo.fcm.controller;

import com.example.demo.enums.FcmCategory;
import com.example.demo.fcm.dto.NoticeDto;
import com.example.demo.fcm.entity.Notice;
import com.example.demo.fcm.repository.NoticeRepository;
import com.example.demo.users.entity.Users;
import com.example.demo.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeApiController {

    private final NoticeRepository noticeRepo;
    private final UserRepository    userRepo;

    /**
     * 내 알림 조회 (전체 or 카테고리별)
     * GET /api/notices?category=DAILY
     */
    @GetMapping
    public List<NoticeDto> list(
            @RequestParam(required = false) FcmCategory category,
            Principal principal
    ) {
        Users me = userRepo.findByUuid(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        List<Notice> notices = (category == null)
                ? noticeRepo.findByUserAndDeletedFalseOrderBySentAtDesc(me)
                : noticeRepo.findByUserAndCategoryAndDeletedFalseOrderBySentAtDesc(me, category);

        return notices.stream()
                .map(NoticeDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 내 알림 삭제 (soft-delete)
     * DELETE /api/notices/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            Principal principal
    ) {
        Users me = userRepo.findByUuid(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        Notice notice = noticeRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!notice.getUser().getId().equals(me.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        notice.setDeleted(true);
        notice.setDeletedAt(java.time.LocalDateTime.now());
        noticeRepo.save(notice);
    }

    // 읽음 표시
    @PostMapping("/mark-read")
    public void markAllRead(Principal principal) {
        Users me = userRepo.findByUuid(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        noticeRepo.findByUserAndDeletedFalseAndReadFlagFalse(me)
                .forEach(n -> {
                    n.setReadFlag(true);
                    noticeRepo.save(n);
                });
    }
}
