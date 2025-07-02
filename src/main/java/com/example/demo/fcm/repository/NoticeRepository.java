package com.example.demo.fcm.repository;

import com.example.demo.enums.FcmCategory;
import com.example.demo.fcm.entity.Notice;
import com.example.demo.users.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findByUserAndDeletedFalseOrderBySentAtDesc(Users user);
    Page<Notice> findByUserAndDeletedFalse(Users user, Pageable pageable);
    List<Notice> findByUserAndCategoryAndDeletedFalseOrderBySentAtDesc(Users user, FcmCategory category);

    // 읽지 않고, 삭제되지 않은 알림 개수
    long  countByUserAndDeletedFalseAndReadFlagFalse(Users user);

    // 읽지 않은(unread) 알림만 조회
    List<Notice> findByUserAndDeletedFalseAndReadFlagFalse(Users user);
}
