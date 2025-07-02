package com.example.demo.survey.service;

import com.example.demo.enums.AgeGroup;
import com.example.demo.survey.entity.RecordSurvey;
import com.example.demo.survey.repository.RecordSurveyRepository;
import com.example.demo.exception.SurveyNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 로깅 추가
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 기록 문진(RecordSurvey) 엔티티에 대한 비즈니스 로직을 제공하는 서비스입니다.
 * 문진 질문의 조회, 저장, 수정, 삭제(soft delete), 연령대별 필터링 등의 기능을 담당합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j // 로깅 활성화
public class RecordSurveyService {

    private final RecordSurveyRepository recordSurveyRepository;

    /**
     * 시스템에 존재하는 모든 삭제되지 않은 기록 문진 질문 목록을 조회합니다.
     *
     * @return 삭제되지 않은 RecordSurvey 엔티티 리스트
     */
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션으로 설정
    public List<RecordSurvey> findAll() {
        log.info("모든 삭제되지 않은 기록 문진 조회");
        return recordSurveyRepository.findAllByDeletedFalse();
    }

    /**
     * 시스템에 존재하는 삭제되지 않은 기록 문진 질문 목록을 페이징하여 조회합니다.
     *
     * @param pageable 페이징 및 정렬 정보를 포함하는 Pageable 객체
     * @return 페이징 처리된 RecordSurvey 엔티티 Page 객체
     */
    @Transactional(readOnly = true)
    public Page<RecordSurvey> findAll(Pageable pageable) {
        log.info("모든 삭제되지 않은 기록 문진 페이징 조회 - 페이지: {}, 사이즈: {}", pageable.getPageNumber(), pageable.getPageSize());
        return recordSurveyRepository.findAllByDeletedFalse(pageable);
    }

    /**
     * 특정 연령대에 해당하는 삭제되지 않은 기록 문진 질문 목록을 조회합니다.
     *
     * @param ageGroup 조회할 연령대 (예: AgeGroup.CHILD, AgeGroup.TEENAGER)
     * @return 해당 연령대에 속하는 RecordSurvey 엔티티 리스트
     */
    @Transactional(readOnly = true)
    public List<RecordSurvey> getSurveysByAgeGroup(AgeGroup ageGroup) {
        log.info("연령대 '{}'에 해당하는 기록 문진 조회", ageGroup.getDisplayName());
        return recordSurveyRepository.findByAgeGroupAndDeletedFalse(ageGroup);
    }

    /**
     * 특정 연령대에 해당하는 삭제되지 않은 기록 문진 질문 목록을 페이징하여 조회합니다.
     *
     * @param ageGroup 조회할 연령대
     * @param pageable 페이징 및 정렬 정보를 포함하는 Pageable 객체
     * @return 해당 연령대에 속하는 페이징 처리된 RecordSurvey 엔티티 Page 객체
     */
    @Transactional(readOnly = true)
    public Page<RecordSurvey> findByAgeGroup(AgeGroup ageGroup, Pageable pageable) {
        log.info("연령대 '{}' 기록 문진 페이징 조회 - 페이지: {}, 사이즈: {}", ageGroup.getDisplayName(), pageable.getPageNumber(), pageable.getPageSize());
        return recordSurveyRepository.findByAgeGroupAndDeletedFalse(ageGroup, pageable);
    }

    /**
     * 새로운 기록 문진 질문을 저장합니다.
     * 이 메서드는 주로 관리자 페이지에서 새로운 문진 질문을 생성할 때 사용됩니다.
     *
     * @param survey 저장할 RecordSurvey 엔티티
     * @return 저장된 RecordSurvey 엔티티
     */
    @Transactional
    public RecordSurvey save(RecordSurvey survey) {
        log.info("새로운 기록 문진 저장: 질문 = '{}'", survey.getQuestion());
        return recordSurveyRepository.save(survey);
    }

    /**
     * 주어진 ID에 해당하는 기록 문진 질문을 조회합니다.
     * 이 메서드는 문진이 존재하지 않을 경우 {@link SurveyNotFoundException}을 발생시킵니다.
     *
     * @param id 조회할 문진의 고유 ID
     * @return 조회된 RecordSurvey 엔티티
     * @throws SurveyNotFoundException 해당 ID의 문진을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public RecordSurvey findById(Long id) {
        log.info("ID '{}'로 기록 문진 조회", id);
        return recordSurveyRepository.findById(id)
                .orElseThrow(() -> new SurveyNotFoundException("기록 문진을 찾을 수 없습니다. ID: " + id));
    }

    /**
     * 주어진 ID에 해당하는 기록 문진 질문을 논리적으로 삭제(soft delete)합니다.
     * 실제 데이터는 삭제되지 않고 'deleted' 상태만 'true'로 변경됩니다.
     *
     * @param id 삭제할 문진의 고유 ID
     * @throws SurveyNotFoundException 해당 ID의 문진을 찾을 수 없는 경우
     */
    @Transactional
    public void delete(Long id) {
        log.info("ID '{}' 기록 문진 삭제 요청 (soft delete)", id);
        RecordSurvey survey = findById(id); // 먼저 문진을 찾고
        survey.markAsDeleted();             // 삭제 상태로 마크
        recordSurveyRepository.save(survey); // 변경된 상태를 저장
        log.info("ID '{}' 기록 문진이 성공적으로 soft delete 처리되었습니다.", id);
    }
}