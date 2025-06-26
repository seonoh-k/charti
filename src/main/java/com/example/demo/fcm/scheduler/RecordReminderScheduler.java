package com.example.demo.fcm.scheduler;

import com.example.demo.fcm.service.FcmService;
import com.example.demo.enums.FcmCategory;
import com.example.demo.survey.repository.DailyAnswerRepository;
import com.example.demo.survey.repository.RecordAnswerRepository;
import com.example.demo.users.entity.Users;
import com.example.demo.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RecordReminderScheduler {

    private final RecordAnswerRepository answerRepo;
    private final UserRepository userRepo;
    private final FcmService fcmService;

    /**
     * 매일 오후 9시(Asia/Seoul)마다 실행
     * 0 0 21 -> 초 분 시
     */
    @Scheduled(cron = "00 36 16 * * *", zone = "Asia/Seoul")
    @Transactional
    public void remindMissedDailyPerChild() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end   = today.plusDays(1).atStartOfDay();

        userRepo.findAll().forEach(user -> {
            if (user.getMember() == null) return;

            user.getMember().getChildren().stream()
                    // 오늘 답변이 없는 자녀만 필터
                    .filter(child -> !answerRepo.existsByChildIdAndCreatedAtBetween(
                            child.getId(), start, end))
                    .forEach(child -> {
                        String title = child.getName() + "님, 오늘의 기록 문진을 잊으셨네요!";
                        String body  = "지금 바로 문진을 완료해주세요.";
                        String url   = "http://localhost:8080/survey/record?childId=" + child.getId();

                        fcmService.sendNotification(
                                null, // sender
                                user, // recipient
                                title,
                                body,
                                FcmCategory.RECORD, // 또는 FcmCategory.SPECIAL 등
                                url
                        );
                    });
        });
    }
}
