package com.example.demo.fcm.repository;

import com.example.demo.fcm.entity.FcmSendHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FcmSendHistoryRepository extends JpaRepository<FcmSendHistory, Long> {
    // 기본적인 CRUD 메서드 사용 가능
}