package com.example.demo.fcm.repository;

import com.example.demo.fcm.entity.UserFcmToken;
import com.example.demo.users.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자별 FCM 토큰 관리 리포지토리
 */
public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, Long> {

    /**
     * 특정 유저가 가진 모든 토큰 조회
     */
    List<UserFcmToken> findByUser(Users user);

    /**
     * 특정 토큰으로 조회
     */
    Optional<UserFcmToken> findByFcmToken(String fcmToken);

    /**
     * 특정 유저의 토큰 중 활성화된 것만 조회
     */
    List<UserFcmToken> findByUserAndIsActiveTrue(Users user);

    /**
     * 전체에서 활성화된 토큰만 조회
     */
    List<UserFcmToken> findByIsActiveTrue();
}
