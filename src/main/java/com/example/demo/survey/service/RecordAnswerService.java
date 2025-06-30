package com.example.demo.survey.service;

import com.example.demo.dto.paging.PagingRequest;
import com.example.demo.dto.paging.PagingResponse;
import com.example.demo.dto.paging.PagingResultDTO;
import com.example.demo.exception.*;
import com.example.demo.service.BaseService;
import com.example.demo.survey.dto.*;
import com.example.demo.survey.entity.RecordAnswer;
import com.example.demo.survey.entity.RecordSurvey;
import com.example.demo.survey.repository.RecordAnswerRepository;
import com.example.demo.survey.repository.RecordSurveyRepository;
import com.example.demo.users.dto.ChildHistorySummaryDto;
import com.example.demo.users.dto.ChildRecordCountDto;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Member;
import com.example.demo.users.repository.MemberRepository;
import com.example.demo.users.service.ChildService;
import com.example.demo.service.PointService;
import com.example.demo.users.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 기록 문진 답변 관련 비즈니스 로직을 담당하는 서비스 클래스입니다.
 */
@Service
@Slf4j
public class RecordAnswerService extends BaseService<RecordAnswer, RecordAnswerRepository> {

    private final ChildService childService;
    private final RecordSurveyRepository recordSurveyRepository;
    private final PointService pointService;
    private final MemberRepository memberRepository;

    public RecordAnswerService(
            RecordAnswerRepository recordAnswerRepository,
            ChildService childService,
            RecordSurveyRepository recordSurveyRepository,
            PointService pointService,
            MemberRepository memberRepository) {
        super(recordAnswerRepository);
        this.childService = childService;
        this.recordSurveyRepository = recordSurveyRepository;
        this.pointService = pointService;
        this.memberRepository = memberRepository;
    }

    /**
     * 작성자의 전체 기록 문진 답변을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<RecordAnswer> getAnswersByWriter(Member writer) {
        log.debug("작성자 '{}'의 모든 기록 문진 답변 조회", writer.getId());
        return repository.findByWriterAndDeletedFalse(writer);
    }

    /**
     * 작성자와 자녀 기준으로 기록 문진 답변을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<RecordAnswer> getAnswersByWriterAndChild(Member writer, Child child) {
        log.debug("작성자 '{}'와 자녀 '{}'의 기록 문진 답변 조회", writer.getId(), child.getId());
        return repository.findByWriterAndChildAndDeletedFalse(writer, child);
    }

    /**
     * 단일 문진 답변을 소프트 삭제 처리합니다.
     */
    @Transactional
    public void softDelete(Long id) {
        RecordAnswer answer = repository.findById(id)
                .orElseThrow(() -> new RecordAnswerNotFoundException("삭제할 답변을 찾을 수 없습니다. ID: " + id));
        answer.markAsDeleted();
        repository.save(answer);
    }

    /**
     * 단일 문진 답변 텍스트를 수정합니다.
     */
    @Transactional
    public void updateAnswerText(Long answerId, String newAnswer) {
        RecordAnswer answer = repository.findById(answerId)
                .orElseThrow(() -> new RecordAnswerNotFoundException("수정할 답변을 찾을 수 없습니다. ID: " + answerId));
        answer.setAnswer(newAnswer);
        repository.save(answer);
    }

    /**
     * 보호자 ID 기준으로 자녀별 기록 문진 이력 요약 정보를 반환합니다.
     */
    @Transactional(readOnly = true)
    public List<ChildHistorySummaryDto> getChildrenWithHistorySummary(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("해당 ID의 멤버를 찾을 수 없습니다. ID: " + memberId));

        List<Child> children = childService.getChildrenByMember(member);
        if (children.isEmpty()) return new ArrayList<>();

        Map<Long, Long> countsByChildId = repository.countDistinctRecordDatesByChildren(children).stream()
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
    public Page<RecordDateSummaryDto> getRecordDatesPagedByChild(Long childId, Pageable pageable) {
        Child child = childService.findById(childId);
        Page<Object[]> rawPage = repository.findDistinctRecordDatesByChild(child, pageable);
        return rawPage.map(row -> new RecordDateSummaryDto((Date) row[0], (Long) row[1]));
    }

    /**
     * 특정 자녀의 특정 날짜에 대한 질문/답변 목록을 반환합니다.
     */
    @Transactional(readOnly = true)
    public List<RecordAnswerResponse> getQuestionsAndAnswersForDate(Long childId, LocalDate date) {
        Child child = childService.findById(childId);
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return repository.findByChildAndCreatedAtBetween(child, start, end)
                .stream().map(RecordAnswerResponse::fromEntity).collect(Collectors.toList());
    }

    /**
     * 특정 자녀가 오늘 문진을 제출했는지 여부를 확인합니다.
     */
    @Transactional(readOnly = true)
    public boolean hasAnsweredToday(Child child) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        return repository.existsByChildAndCreatedAtBetween(child, start, end);
    }

    /**
     * 보호자가 제출한 문진 답변을 저장하고 포인트 지급을 처리합니다.
     */
    @Transactional
    public void submitRecordAnswers(Member member, List<RecordSurveyAnswerDto> requestList) {
        if (requestList == null || requestList.isEmpty()) {
            throw new IllegalArgumentException("문진 답변이 비어있습니다.");
        }

        Long childId = requestList.get(0).getChildId();
        Child child = childService.findById(childId);

        if (!child.getParent().getId().equals(member.getId())) {
            throw new UnauthorizedAccessException("유효하지 않거나 접근 권한이 없는 자녀 정보입니다.");
        }

        if (hasAnsweredToday(child)) {
            throw new DuplicateSurveySubmissionException("선택하신 자녀는 오늘 이미 기록 문진을 작성했습니다.");
        }

        List<RecordAnswer> answersToSave = new ArrayList<>();
        for (RecordSurveyAnswerDto req : requestList) {
            RecordSurvey survey = recordSurveyRepository.findById(req.getQuestionId())
                    .orElseThrow(() -> new RecordSurveyQuestionNotFoundException("존재하지 않는 질문입니다. ID: " + req.getQuestionId()));

            RecordAnswer answer = new RecordAnswer();
            answer.setWriter(member);
            answer.setChild(child);
            answer.setSurvey(survey);
            answer.setQuestion(survey.getQuestion());
            answer.setAnswer(req.getText());
            answer.setAnswered(true);
            answersToSave.add(answer);
        }
        repository.saveAll(answersToSave);

        pointService.giveRecordSurveyPointIfEligible(member, child);
    }

    /**
     * 특정 자녀의 특정 날짜에 기록된 답변을 수정합니다. (관리자용)
     */
    @Transactional
    public void updateRecordAnswers(Long childId, LocalDate recordDate, List<UpdateAnswerRequestDto> updatedAnswers) {
        LocalDateTime start = recordDate.atStartOfDay();
        LocalDateTime end = recordDate.atTime(LocalTime.MAX);

        List<RecordAnswer> existingAnswers = repository.findByChild_IdAndCreatedAtBetween(childId, start, end);
        if (existingAnswers.isEmpty()) {
            throw new RecordHistoryNotFoundException("해당 날짜에 기록된 문진 답변이 없습니다.");
        }

        Map<Long, RecordAnswer> answerMap = existingAnswers.stream()
                .collect(Collectors.toMap(ra -> ra.getSurvey().getSurveyId(), ra -> ra));

        List<RecordAnswer> answersToUpdate = new ArrayList<>();
        for (UpdateAnswerRequestDto updateDto : updatedAnswers) {
            RecordAnswer answerToUpdate = answerMap.get(updateDto.getQuestionId());
            if (answerToUpdate == null) {
                throw new RecordAnswerNotFoundException("질문 ID " + updateDto.getQuestionId() + " 에 해당하는 기존 답변을 찾을 수 없습니다.");
            }
            answerToUpdate.setAnswer(updateDto.getAnswer());
            answersToUpdate.add(answerToUpdate);
        }
        repository.saveAll(answersToUpdate);
    }

    /**
     * 특정 자녀의 특정 날짜에 기록된 답변을 모두 삭제합니다. (관리자용)
     */
    @Transactional
    public void deleteRecordAnswers(Long childId, LocalDate recordDate) {
        LocalDateTime start = recordDate.atStartOfDay();
        LocalDateTime end = recordDate.atTime(LocalTime.MAX);

        List<RecordAnswer> answersToDelete = repository.findByChild_IdAndCreatedAtBetween(childId, start, end);
        if (answersToDelete.isEmpty()) {
            throw new RecordHistoryNotFoundException("삭제할 문진 기록이 존재하지 않습니다.");
        }
        repository.deleteAll(answersToDelete);
    }

    /**
     * 특정 자녀의 기록 문진 답변 이력을 페이지 단위로 조회합니다.
     */
    @Transactional(readOnly = true)
    public PagingResponse<RecordAnswerResponse> getRecordAnswersPageByChild(Long childId, PagingRequest pagingRequest) {
        Child child = childService.findById(childId);

        Pageable pageable = PageRequest.of(
                pagingRequest.getPage() != null ? pagingRequest.getPage() : 0,
                pagingRequest.getSize() != null ? pagingRequest.getSize() : 5,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<RecordAnswer> result = repository.findByChildAndDeletedFalse(child, pageable);

        PagingResultDTO<RecordAnswerResponse, RecordAnswer> pagingResultDTO =
                new PagingResultDTO<>(result, RecordAnswerResponse::fromEntity);

        return PagingResponse.from(pagingResultDTO);
    }

    public List<RecordAnswerResponse> getPagedAnswerList(Long childId) {
        Child child = childService.findById(childId);

        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<RecordAnswer> result = repository.findByChildAndDeletedFalse(child, pageable);

        List<RecordAnswerResponse> rAnswerList = new ArrayList<>();
        for(RecordAnswer answer : result.getContent()) {
            rAnswerList.add(new RecordAnswerResponse(answer));
        }

        return rAnswerList;
    }
}
