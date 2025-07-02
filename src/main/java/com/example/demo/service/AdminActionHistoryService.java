package com.example.demo.service;

import com.example.demo.entity.AdminActionHistory;
import com.example.demo.repository.AdminActionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminActionHistoryService {


    private final AdminActionHistoryRepository adminActionHistoryRepository;

    /**
     * 비동기로, 그리고 본 로직과 분리된 새 트랜잭션에서 감사 로그를 저장합니다.
     */
    @Async  // 별도 스레드에서 실행 :contentReference[oaicite:1]{index=1}
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(AdminActionHistory entity) {
        adminActionHistoryRepository.save(entity);  // RDB에 INSERT
    }
}
