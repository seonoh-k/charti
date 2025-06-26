package com.example.demo.fcm.repository;

import com.example.demo.fcm.entity.FcmSendHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FcmSendHistoryRepository
        extends JpaRepository<FcmSendHistory, Long>, JpaSpecificationExecutor<FcmSendHistory> {
}