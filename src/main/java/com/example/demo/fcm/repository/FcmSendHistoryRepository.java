package com.example.demo.fcm.repository;

import com.example.demo.fcm.entity.FcmSendHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FcmSendHistoryRepository
        extends JpaRepository<FcmSendHistory, Long>, JpaSpecificationExecutor<FcmSendHistory> {

    // groupId로 시작하는 targetCondition, sender 이름과 limit로만 조회
    @Query("SELECT f FROM FcmSendHistory f WHERE f.sender.name = :senderName AND f.targetCondition LIKE 'groupId=%' ORDER BY f.sentAt DESC")
    List<FcmSendHistory> findGroupNoticesBySenderName(@Param("senderName") String senderName, Pageable pageable);

}