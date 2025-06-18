package com.example.demo.survey.service;

import com.example.demo.enums.AgeGroup;
import com.example.demo.survey.entity.RecordSurvey;
import com.example.demo.survey.repository.RecordSurveyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecordSurveyService {

    private final RecordSurveyRepository recordSurveyRepository;

    /** 전체(삭제되지 않은) 문진 */
    public List<RecordSurvey> findAll() {
        return recordSurveyRepository.findAllByDeletedFalse();
    }

    /** 전체(삭제되지 않은) 문진 + 페이징 */
    public Page<RecordSurvey> findAll(Pageable pageable) {
        return recordSurveyRepository.findAllByDeletedFalse(pageable);
    }

    /** 연령대 필터 */
    public List<RecordSurvey> getSurveysByAgeGroup(AgeGroup ageGroup) {
        return recordSurveyRepository.findByAgeGroupAndDeletedFalse(ageGroup);
    }

    /** 연령대 필터 + 페이징 */
    public Page<RecordSurvey> findByAgeGroup(AgeGroup ageGroup, Pageable pageable) {
        return recordSurveyRepository.findByAgeGroupAndDeletedFalse(ageGroup, pageable);
    }

    // (관리자) 새로운 문진 저장
    @Transactional
    public RecordSurvey save(RecordSurvey survey) {
        return recordSurveyRepository.save(survey);
    }

    // (관리자/공통) ID로 문진 조회
    public RecordSurvey findById(Long id) {
        return recordSurveyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("문진을 찾을 수 없습니다. id=" + id));
    }

    // (관리자) 문진 삭제 (soft delete)
    @Transactional
    public void delete(Long id) {
        RecordSurvey survey = findById(id);
        survey.markAsDeleted();
    }
}
