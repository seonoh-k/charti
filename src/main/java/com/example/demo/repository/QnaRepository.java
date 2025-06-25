package com.example.demo.repository;

import com.example.demo.entity.Qna;
import com.example.demo.enums.QnaCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QnaRepository extends JpaRepository<Qna, Long> {
    Page<Qna> findAllByDeletedFalse(Pageable pageable);
    Page<Qna> findByCategory(QnaCategory category, Pageable pageable);
}
