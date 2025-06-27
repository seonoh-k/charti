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
    Page<Qna> findAllByCategory(QnaCategory category, Pageable pageable);
    Page<Qna> findByCategoryAndDeletedFalse(QnaCategory category, Pageable pageable);
    Page<Qna> findAllById(Long id, Pageable pageable);

    Page<Qna> findAllByIdAndCategory(Long id, QnaCategory category, Pageable pageable);
}
