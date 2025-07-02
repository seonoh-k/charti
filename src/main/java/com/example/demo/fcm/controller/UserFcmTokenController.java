package com.example.demo.fcm.controller;

import com.example.demo.fcm.dto.UserFcmTokenRequest;
import com.example.demo.fcm.entity.UserFcmToken;
import com.example.demo.fcm.repository.UserFcmTokenRepository;
import com.example.demo.users.entity.Users;
import com.example.demo.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/user/fcm-token")
public class UserFcmTokenController {

    private final UserFcmTokenRepository tokenRepository;
    private final UserRepository usersRepository;

    @PostMapping
    public ResponseEntity<?> saveToken(@RequestBody UserFcmTokenRequest request,
                                       Principal principal) {
        try {
            String username = principal.getName(); // JWT에서 꺼낸 사용자 이름
            log.info("📡 FCM 토큰 저장 요청 - username: {}, token: {}", username, request.getFcmToken());

            Users user = usersRepository.findByUuid(username)
                    .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

            // 이미 등록된 토큰인지 확인
            UserFcmToken token = tokenRepository.findByFcmToken(request.getFcmToken())
                    .orElse(UserFcmToken.builder()
                            .user(user)
                            .fcmToken(request.getFcmToken())
                            .build());

            token.setUser(user);
            token.setActive(true);
            token.setLastUsedAt(LocalDateTime.now());

            tokenRepository.save(token);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("❌ FCM 토큰 저장 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("토큰 저장 실패");
        }
    }
}
