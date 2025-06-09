package com.example.demo.repository;

import com.example.demo.entity.RecordAnswer;
import com.example.demo.entity.Member;
import com.example.demo.entity.Child;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecordAnswerRepository extends JpaRepository<RecordAnswer, Long> {

    // 본인이 작성한 모든 답변 조회 (소프트딜리트 제외)
    List<RecordAnswer> findByWriterAndDeletedFalse(Member writer);

    // 본인이 특정 자녀에 대해 작성한 답변 조회
    List<RecordAnswer> findByWriterAndChildAndDeletedFalse(Member writer, Child child);
}
