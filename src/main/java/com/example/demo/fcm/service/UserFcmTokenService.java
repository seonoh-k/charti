package com.example.demo.fcm.service;

import com.example.demo.fcm.dto.UserFcmTokenRequest;
import com.example.demo.fcm.entity.UserFcmToken;
import com.example.demo.fcm.repository.UserFcmTokenRepository;
import com.example.demo.users.entity.Users;
import com.example.demo.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserFcmTokenService {

    private final UserFcmTokenRepository userFcmTokenRepository;
    private final UserRepository usersRepository;

    @Transactional
    public void saveFcmToken(Long userId, UserFcmTokenRequest request) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. ID: " + userId));

        userFcmTokenRepository.findByFcmToken(request.getFcmToken())
                .ifPresentOrElse(
                        existing -> {
                            existing.setLastUsedAt(LocalDateTime.now());
                            existing.setActive(true);
                        },
                        () -> {
                            UserFcmToken token = UserFcmToken.builder()
                                    .user(user)
                                    .fcmToken(request.getFcmToken())
                                    .lastUsedAt(LocalDateTime.now())
                                    .isActive(true)
                                    .build();
                            userFcmTokenRepository.save(token);
                        }
                );
    }
}
