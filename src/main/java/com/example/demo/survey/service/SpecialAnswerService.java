// com.example.demo.survey.service.SpecialAnswerService.java
package com.example.demo.survey.service;

import com.example.demo.enums.SurveyCategory;
import com.example.demo.exception.RecordAnswerNotFoundException;
import com.example.demo.exception.RecordHistoryNotFoundException;
import com.example.demo.survey.dto.*;
import com.example.demo.survey.entity.SpecialAnswer;
import com.example.demo.survey.entity.SpecialSurvey;
import com.example.demo.enums.AgeGroup;
import com.example.demo.survey.entity.SurveySet;
import com.example.demo.survey.repository.SpecialAnswerRepository;
import com.example.demo.survey.repository.SpecialSurveyRepository;
import com.example.demo.survey.repository.SurveySetRepository;
import com.example.demo.users.dto.ChildHistorySummaryDto;
import com.example.demo.users.dto.ChildRecordCountDto;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Member;
import com.example.demo.users.exception.UserNotFoundException;
import com.example.demo.users.repository.ChildRepository;
import com.example.demo.users.repository.MemberRepository;
import com.example.demo.users.service.ChildService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpecialAnswerService {
    private final SpecialAnswerRepository answerRepo;
    private final ChildService childService;
    private final SurveySetRepository surveySetRepo;
    private final SpecialSurveyRepository surveyRepo;
    private final ChildRepository childRepo;
    private final MemberRepository memberRepo;

    @Transactional
    public void saveAnswers(Long childId,
                            Long setId,
                            List<SurveySetSubmitRequestDto.AnswerDto> answers) {
        Child child = childService.findById(childId);
        SurveySet set = surveySetRepo.findById(setId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세트: " + setId));
        List<SpecialSurvey> surveys = set.getSpecialSurveys();

        if (surveys.size() != answers.size()) {
            throw new IllegalArgumentException("문진 항목 수와 응답 수 불일치");
        }

        Map<Long, Integer> answerMap = answers.stream()
                .collect(Collectors.toMap(SurveySetSubmitRequestDto.AnswerDto::getSurveyId,
                        SurveySetSubmitRequestDto.AnswerDto::getAnswerValue));

        for (int i = 0; i < surveys.size(); i++) {
            SpecialSurvey s = surveys.get(i);
            int idx = answerMap.get(s.getId());
            String text = switch(idx) {
                case 1 -> s.getAnswer1();
                case 2 -> s.getAnswer2();
                case 3 -> s.getAnswer3();
                case 4 -> s.getAnswer4();
                case 5 -> s.getAnswer5();
                default -> throw new IllegalArgumentException("1~5 사이 값 필요");
            };
            SpecialAnswer a = new SpecialAnswer();
            a.setSurveySet(set);
            a.setChild(child);
            a.setAgeGroup(s.getAgeGroup());
            a.setCategory(s.getCategory());
            a.setQuestion(s.getQuestion());
            a.setAnswer(text);
            answerRepo.save(a);
        }
    }

    // [신규 메소드 추가] - 저장된 SpecialAnswer 엔티티 목록을 반환
    @Transactional
    public List<SpecialAnswer> saveAndGetAnswers(
            Long childId,
            AgeGroup ageGroup,
            SurveyCategory category,
            List<SpecialSurveyRequestDto.AnswerDto> answers
    ) {
        // 1) Child 조회
        Child child = childRepo.findById(childId)
                .orElseThrow(() -> new EntityNotFoundException("자녀를 찾을 수 없습니다: " + childId));

        List<SpecialAnswer> saved = new ArrayList<>();
        for (SpecialSurveyRequestDto.AnswerDto dto : answers) {
            // 2) SurveySet 조회 (FK)
            SurveySet set = surveySetRepo.findById(dto.getSurveySetId())
                    .orElseThrow(() -> new EntityNotFoundException("SurveySet을 찾을 수 없습니다: " + dto.getSurveySetId()));

            // 3) SpecialAnswer 엔티티 채우기
            SpecialAnswer ans = new SpecialAnswer();
            ans.setChild(child);
            ans.setSurveySet(set);
            ans.setAgeGroup(ageGroup);
            ans.setCategory(category);
            ans.setQuestion(dto.getQuestion());
            ans.setAnswer(dto.getAnswerText());

            // 4) 저장
            saved.add(answerRepo.save(ans));
        }
        return saved;
    }

    @Transactional
    public List<SpecialAnswer> saveAndGetAnswers2(
            Long childId,
            SurveySet set,
            AgeGroup ageGroup,
            SurveyCategory category,
            List<SurveySetSubmitRequestDto.AnswerDto> answers
    ) {
        // 1) Child 조회
        Child child = childRepo.findById(childId)
                .orElseThrow(() -> new EntityNotFoundException("자녀를 찾을 수 없습니다: " + childId));

        Map<Long, Integer> answerMap = answers.stream()
                .collect(Collectors.toMap(SurveySetSubmitRequestDto.AnswerDto::getSurveyId,
                        SurveySetSubmitRequestDto.AnswerDto::getAnswerValue));

        List<SpecialAnswer> saved = new ArrayList<>();
        for (SurveySetSubmitRequestDto.AnswerDto dto : answers) {
            SpecialSurvey s = surveyRepo.findById(dto.getSurveyId()).get();

            int idx = answerMap.get(s.getId());
            String text = switch (idx) {
                case 1 -> s.getAnswer1();
                case 2 -> s.getAnswer2();
                case 3 -> s.getAnswer3();
                case 4 -> s.getAnswer4();
                case 5 -> s.getAnswer5();
                default -> throw new IllegalArgumentException("1~5 사이 값 필요");
            };

            // 3) SpecialAnswer 엔티티 채우기
            SpecialAnswer ans = new SpecialAnswer();
            ans.setChild(child);
            ans.setSurveySet(set);
            ans.setAgeGroup(ageGroup);
            ans.setCategory(category);
            ans.setQuestion(dto.getQuestion());
            ans.setAnswer(text);

            // 4) 저장
            saved.add(answerRepo.save(ans));
        }
        return saved;
    }

    // 답변 값(1~5)에 해당하는 답변 텍스트를 가져오는 헬퍼 메소드
    private String getAnswerTextByValue(SpecialSurvey survey, int value) {
        return switch (value) {
            case 1 -> survey.getAnswer1();
            case 2 -> survey.getAnswer2();
            case 3 -> survey.getAnswer3();
            case 4 -> survey.getAnswer4();
            case 5 -> survey.getAnswer5();
            default -> throw new IllegalArgumentException("Invalid answer value: " + value);
        };
    }


    public List<SpecialAnswer> findByChild(Long childId) {
        return answerRepo.findByChildIdAndDeletedFalseOrderByCreatedAtDesc(childId);
    }

    @Transactional
    public void updateAnswerValue(Long id, int newValue) {
        SpecialAnswer a = answerRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("답변없음 "+id));

        // 1) 이 답변이 속한 SurveySet 로딩
        SurveySet set = surveySetRepo.findById(a.getSurveySet().getSetId())
                .orElseThrow(() -> new EntityNotFoundException("세트 없음: " + a.getSurveySet().getSetId()));

        // 2) 기존 answer 엔티티에 저장된 question 텍스트로, 해당 문진 항목 객체를 찾음
        SpecialSurvey s = set.getSpecialSurveys().stream()
                .filter(ss -> ss.getQuestion().equals(a.getQuestion()))
                .findFirst()
                .orElseThrow(() ->
                        new EntityNotFoundException("문진 항목 없음: " + a.getQuestion())
                );

        // 3) 선택된 숫자(newValue)에 따라 새 텍스트 매핑
        String newText = switch (newValue) {
            case 1 -> s.getAnswer1();
            case 2 -> s.getAnswer2();
            case 3 -> s.getAnswer3();
            case 4 -> s.getAnswer4();
            case 5 -> s.getAnswer5();
            default -> throw new IllegalArgumentException("1~5 사이 값 필요");
        };

        // 4) 엔티티에 반영 후 저장
        a.setAnswer(newText);
        answerRepo.save(a);
    }

    @Transactional
    public void deleteAnswer(Long id) {
        SpecialAnswer a = answerRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("답변없음 "+id));
        a.markAsDeleted();
        answerRepo.save(a);
    }

    /**
     * 보호자 ID 기준으로 자녀별 기록 문진 이력 요약 정보를 반환합니다.
     */
    @Transactional(readOnly = true)
    public List<ChildHistorySummaryDto> getChildrenWithHistorySummary(Long memberId) {
        Member member = memberRepo.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("해당 ID의 멤버를 찾을 수 없습니다. ID: " + memberId));

        List<Child> children = childService.getChildrenByMember(member);
        if (children.isEmpty()) return new ArrayList<>();

        Map<Long, Long> countsByChildId = answerRepo.countDistinctSpecialDatesByChildren(children).stream()
                .collect(Collectors.toMap(ChildRecordCountDto::getChildId, ChildRecordCountDto::getRecordCount));

        return children.stream()
                .map(child -> ChildHistorySummaryDto.builder()
                        .childId(child.getId())
                        .childName(child.getName())
                        .childAge(child.getAge())
                        .totalRecordDatesCount(countsByChildId.getOrDefault(child.getId(), 0L))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 특정 자녀의 기록 문진 날짜 목록을 페이징하여 조회합니다.
     */
    @Transactional(readOnly = true)
    public Page<SpecialDateSummaryDto> getSpecialDatesPagedByChild(Long childId, Pageable pageable) {
        Child child = childService.findById(childId);
        Page<Object[]> rawPage = answerRepo.findDistinctSpecialDatesByChild(child, pageable);
        return rawPage.map(row -> new SpecialDateSummaryDto((Date) row[0], (Long) row[1]));
    }

    /**
     * 특정 자녀의 특정 날짜에 대한 질문/답변 목록을 반환합니다.
     */
    @Transactional(readOnly = true)
    public List<SpecialAnswerResponse> getQuestionsAndAnswersForDate(Long childId, LocalDate date) {
        Child child = childService.findById(childId);
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return answerRepo.findByChildAndCreatedAtBetween(child, start, end)
                .stream().map(SpecialAnswerResponse::fromEntity).collect(Collectors.toList());
    }

    /**
     * 특정 자녀의 특정 날짜에 기록된 답변을 수정합니다. (관리자용)
     */
    @Transactional
    public void updateSpecialAnswers(Long childId, LocalDate recordDate, List<UpdateAnswerRequestDto> updatedAnswers) {
        LocalDateTime start = recordDate.atStartOfDay();
        LocalDateTime end = recordDate.atTime(LocalTime.MAX);

        List<SpecialAnswer> existingAnswers = answerRepo.findByChild_IdAndCreatedAtBetween(childId, start, end);
        if (existingAnswers.isEmpty()) {
            throw new RecordHistoryNotFoundException("해당 날짜에 기록된 문진 답변이 없습니다.");
        }

        Map<Long, SpecialAnswer> answerMap = existingAnswers.stream()
                .collect(Collectors.toMap(ra -> ra.getSurveySet().getSetId(), ra -> ra));

        List<SpecialAnswer> answersToUpdate = new ArrayList<>();
        for (UpdateAnswerRequestDto updateDto : updatedAnswers) {
            SpecialAnswer answerToUpdate = answerMap.get(updateDto.getQuestionId());
            if (answerToUpdate == null) {
                throw new RecordAnswerNotFoundException("질문 ID " + updateDto.getQuestionId() + " 에 해당하는 기존 답변을 찾을 수 없습니다.");
            }
            answerToUpdate.setAnswer(updateDto.getAnswer());
            answersToUpdate.add(answerToUpdate);
        }
        answerRepo.saveAll(answersToUpdate);
    }

    /**
     * 특정 자녀의 특정 날짜에 기록된 답변을 모두 삭제합니다. (관리자용)
     */
    @Transactional
    public void deleteSpecialAnswers(Long childId, LocalDate recordDate) {
        LocalDateTime start = recordDate.atStartOfDay();
        LocalDateTime end = recordDate.atTime(LocalTime.MAX);

        List<SpecialAnswer> answersToDelete = answerRepo.findByChild_IdAndCreatedAtBetween(childId, start, end);
        if (answersToDelete.isEmpty()) {
            throw new RecordHistoryNotFoundException("삭제할 문진 기록이 존재하지 않습니다.");
        }
        answerRepo.deleteAll(answersToDelete);
    }
}