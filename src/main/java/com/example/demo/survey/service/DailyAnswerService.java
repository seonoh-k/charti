package com.example.demo.survey.service;

import com.example.demo.exception.RecordAnswerNotFoundException;
import com.example.demo.exception.RecordHistoryNotFoundException;
import com.example.demo.survey.dto.*;
import com.example.demo.survey.entity.DailyAnswer;
import com.example.demo.survey.entity.DailySurvey;
import com.example.demo.survey.repository.DailyAnswerRepository;
import com.example.demo.users.dto.ChildHistorySummaryDto;
import com.example.demo.users.dto.ChildRecordCountDto;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Member;
import com.example.demo.users.exception.UserNotFoundException;
import com.example.demo.users.repository.MemberRepository;
import com.example.demo.users.service.ChildService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
public class DailyAnswerService {
    private final DailyAnswerRepository repo;
    private final MemberRepository memberRepo;
    private final ChildService childService;

    public List<DailyAnswer> getAnswersByChild(Long childId) {
        return repo.findByChildIdAndDeletedFalseOrderByCreatedAtDesc(childId);
    }

    public void updateAnswerValue(Long id, int value) {
        DailyAnswer da = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("답변이 없습니다. id=" + id));

        DailySurvey s = da.getSurvey();
        String newText;
        switch (value) {
            case 1: newText = s.getAnswer1(); break;
            case 2: newText = s.getAnswer2(); break;
            case 3: newText = s.getAnswer3(); break;
            case 4: newText = s.getAnswer4(); break;
            case 5: newText = s.getAnswer5(); break;
            default: throw new IllegalArgumentException("유효한 값(1~5)이 아닙니다: " + value);
        }

        da.setAnswer(newText);
        repo.save(da);
    }

    public void deleteAnswer(Long id) {
        DailyAnswer da = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("답변을 찾을 수 없습니다."));
        da.markAsDeleted();  // BaseEntity 의 soft-delete 메서드
        repo.save(da);
    }

    public List<DailyAnswerDto> getPagedAnswerList(Long childId) {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());
        Page<DailyAnswer> ansList = repo.findAllByChildIdAndDeletedFalse(childId, pageable);

        List<DailyAnswerDto> dtoList = new ArrayList<>();
        for(DailyAnswer d : ansList.getContent()) {
            dtoList.add(new DailyAnswerDto(d));
        }

        return dtoList;
    }

    // 특정 자녀가 오늘 데일리 문진을 제출했는지 여부를 확인
    @Transactional(readOnly = true)
    public boolean hasAnsweredToday(Child child) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end   = LocalDate.now().atTime(LocalTime.MAX);
        return repo.existsByChildAndCreatedAtBetween(child, start, end);
    }
    public DailyAnswer save(DailyAnswer da) {
        return repo.save(da);
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

        Map<Long, Long> countsByChildId = repo.countDistinctSpecialDatesByChildren(children).stream()
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
    public Page<DailyDateSummaryDto> getDailyDatesPagedByChild(Long childId, Pageable pageable) {
        Child child = childService.findById(childId);
        Page<Object[]> rawPage = repo.findDistinctSpecialDatesByChild(child, pageable);
        return rawPage.map(row -> new DailyDateSummaryDto((Date) row[0], (Long) row[1]));
    }

    /**
     * 특정 자녀의 특정 날짜에 대한 질문/답변 목록을 반환합니다.
     */
    @Transactional(readOnly = true)
    public List<DailyAnswerResponse> getQuestionsAndAnswersForDate(Long childId, LocalDate date) {
        Child child = childService.findById(childId);
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return repo.findByChildAndCreatedAtBetween(child, start, end)
                .stream().map(DailyAnswerResponse::fromEntity).collect(Collectors.toList());
    }

    /**
     * 특정 자녀의 특정 날짜에 기록된 답변을 수정합니다. (관리자용)
     */
    @Transactional
    public void updateDailyAnswers(Long childId, LocalDate recordDate, List<UpdateAnswerRequestDto> updatedAnswers) {
        LocalDateTime start = recordDate.atStartOfDay();
        LocalDateTime end = recordDate.atTime(LocalTime.MAX);

        List<DailyAnswer> existingAnswers = repo.findByChild_IdAndCreatedAtBetween(childId, start, end);
        if (existingAnswers.isEmpty()) {
            throw new RecordHistoryNotFoundException("해당 날짜에 기록된 문진 답변이 없습니다.");
        }

        Map<Long, DailyAnswer> answerMap = existingAnswers.stream()
                .collect(Collectors.toMap(ra -> ra.getSurvey().getId(), ra -> ra));

        List<DailyAnswer> answersToUpdate = new ArrayList<>();
        for (UpdateAnswerRequestDto updateDto : updatedAnswers) {
            DailyAnswer answerToUpdate = answerMap.get(updateDto.getQuestionId());
            if (answerToUpdate == null) {
                throw new RecordAnswerNotFoundException("질문 ID " + updateDto.getQuestionId() + " 에 해당하는 기존 답변을 찾을 수 없습니다.");
            }
            answerToUpdate.setAnswer(updateDto.getAnswer());
            answersToUpdate.add(answerToUpdate);
        }
        repo.saveAll(answersToUpdate);
    }

    /**
     * 특정 자녀의 특정 날짜에 기록된 답변을 모두 삭제합니다. (관리자용)
     */
    @Transactional
    public void deleteDailyAnswers(Long childId, LocalDate recordDate) {
        LocalDateTime start = recordDate.atStartOfDay();
        LocalDateTime end = recordDate.atTime(LocalTime.MAX);

        List<DailyAnswer> answersToDelete = repo.findByChild_IdAndCreatedAtBetween(childId, start, end);
        if (answersToDelete.isEmpty()) {
            throw new RecordHistoryNotFoundException("삭제할 문진 기록이 존재하지 않습니다.");
        }
        repo.deleteAll(answersToDelete);
    }
}

