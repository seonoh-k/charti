package com.example.demo.fcm.repository;

import com.example.demo.fcm.entity.FcmSendHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FcmSendHistoryRepository extends JpaRepository<FcmSendHistory, Long> {
    // 기본적인 CRUD 메서드 사용 가능
}
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import java.time.LocalDateTime;
//
//public interface FcmSendHistoryRepository extends JpaRepository<FcmSendHistory, Long> {
//    // 특정 기간 동안의 발송 이력을 페이징하여 조회
//    Page<FcmSendHistory> findBySentAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
//
//    // 특정 카테고리의 발송 이력을 페이징하여 조회
//    Page<FcmSendHistory> findByCategory(FcmCategory category, Pageable pageable);
//}