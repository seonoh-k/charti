package com.example.demo.survey.service;

import com.example.demo.enums.AgeGroup;
import com.example.demo.service.BaseService;
import com.example.demo.survey.dto.RecordSurveyAnswerDto;
import com.example.demo.survey.entity.RecordAnswer;
import com.example.demo.survey.entity.RecordSurvey;
import com.example.demo.survey.repository.RecordAnswerRepository;
import com.example.demo.survey.repository.RecordSurveyRepository;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Member;
import com.example.demo.users.repository.MemberRepository;
import com.example.demo.users.service.ChildService;
import com.example.demo.service.PointService;
import com.example.demo.users.dto.ChildHistorySummaryDto;
import com.example.demo.survey.dto.RecordDateSummaryDto;
import com.example.demo.survey.dto.QuestionAnswerPairDto;
import com.example.demo.exception.SurveyNotFoundException;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RecordAnswerService extends BaseService<RecordAnswer, RecordAnswerRepository> {

    private final RecordSurveyService recordSurveyService;
    private final ChildService childService;
    private final RecordSurveyRepository recordSurveyRepository;
    private final PointService pointService;
    private final MemberRepository memberRepository;

    /**
     * RecordAnswerService의 생성자입니다.
     * BaseService의 생성자를 호출하고, 나머지 final 필드들을 초기화합니다.
     *
     * @param recordAnswerRepository RecordAnswer 엔티티를 관리하는 리포지토리
     * @param recordSurveyService 기록 문진 관련 비즈니스 로직을 처리하는 서비스
     * @param childService 자녀 관련 비즈니스 로직을 처리하는 서비스
     * @param recordSurveyRepository RecordSurvey 엔티티를 관리하는 리포지토리
     * @param pointService 포인트 관련 비즈니스 로직을 처리하는 서비스
     * @param memberRepository Member 엔티티를 관리하는 리포지토리
     */
    public RecordAnswerService(
            RecordAnswerRepository recordAnswerRepository,
            RecordSurveyService recordSurveyService,
            ChildService childService,
            RecordSurveyRepository recordSurveyRepository,
            PointService pointService,
            MemberRepository memberRepository) {
        super(recordAnswerRepository);

        this.recordSurveyService = recordSurveyService;
        this.childService = childService;
        this.recordSurveyRepository = recordSurveyRepository;
        this.pointService = pointService;
        this.memberRepository = memberRepository;
    }

    /**
     * 기록 문진 답변을 저장합니다.
     * @param writer 작성자 (회원)
     * @param child 자녀 정보
     * @param dto 질문 ID와 답변 텍스트를 포함한 DTO
     */
    @Transactional
    public void saveAnswer(Member writer, Child child, RecordSurveyAnswerDto dto) {
        log.info("답변 저장 시도: 질문 ID={}, 자녀 ID={}, 작성자 ID={}", dto.getQuestionId(), child.getId(), writer.getId());
        RecordAnswer answer = new RecordAnswer();
        answer.setWriter(writer);
        answer.setChild(child);

        RecordSurvey survey = recordSurveyRepository.findById(dto.getQuestionId())
                .orElseThrow(() -> new EntityNotFoundException("질문을 찾을 수 없습니다. ID: " + dto.getQuestionId()));

        answer.setSurvey(survey);
        answer.setQuestion(survey.getQuestion());
        answer.setAnswer(dto.getText());
        answer.setAnswered(true);

        repository.save(answer);
        log.info("답변 저장 완료: 답변 ID={}", answer.getAnswerId());
    }

    /**
     * 작성자 기준으로 삭제되지 않은 모든 기록 문진 답변을 조회합니다.
     * @param writer 조회할 작성자 (Member 엔티티)
     * @return 해당 작성자가 작성한 RecordAnswer 리스트
     */
    @Transactional(readOnly = true)
    public List<RecordAnswer> getAnswersByWriter(Member writer) {
        log.debug("작성자 '{}'의 모든 기록 문진 답변 조회", writer.getId());
        return repository.findByWriterAndDeletedFalse(writer);
    }

    /**
     * 작성자 및 자녀 기준으로 삭제되지 않은 기록 문진 답변을 조회합니다.
     * @param writer 조회할 작성자 (Member 엔티티)
     * @param child 조회할 자녀 (Child 엔티티)
     * @return 해당 작성자와 자녀가 작성한 RecordAnswer 리스트
     */
    @Transactional(readOnly = true)
    public List<RecordAnswer> getAnswersByWriterAndChild(Member writer, Child child) {
        log.debug("작성자 '{}'와 자녀 '{}'의 기록 문진 답변 조회", writer.getId(), child.getId());
        return repository.findByWriterAndChildAndDeletedFalse(writer, child);
    }

    /**
     * 특정 답변 ID를 기준으로 soft delete 처리합니다.
     * @param id 삭제할 답변의 고유 ID
     * @throws EntityNotFoundException 해당 ID의 답변을 찾을 수 없는 경우
     */
    @Transactional
    public void softDelete(Long id) {
        log.info("답변 ID '{}' soft delete 요청", id);
        RecordAnswer answer = get(id);
        answer.markAsDeleted();
        repository.save(answer);
        log.info("답변 ID '{}' soft delete 완료", id);
    }

    /**
     * 인라인 수정 기능을 위한 답변 수정 메서드입니다.
     * @param answerId 수정할 답변의 ID
     * @param newAnswer 새로운 답변 텍스트
     * @throws EntityNotFoundException 해당 ID의 답변을 찾을 수 없는 경우
     */
    @Transactional
    public void updateAnswerText(Long answerId, String newAnswer) {
        log.info("답변 ID '{}' 텍스트 수정 요청: 새 답변 '{}'", answerId, newAnswer);
        RecordAnswer answer = repository.findById(answerId)
                .orElseThrow(() -> new EntityNotFoundException("답변을 찾을 수 없습니다. ID: " + answerId));
        answer.setAnswer(newAnswer);
        repository.save(answer);
        log.info("답변 ID '{}' 텍스트 수정 완료", answerId);
    }

    /**
     * [기록문진 답변 저장 및 포인트 지급]
     * 사용자가 제출한 기록문진 답변 리스트를 저장하고,
     * 자녀별 하루 1회만 포인트가 지급되도록 중복 체크 후 지급합니다.
     *
     * @param member 현재 로그인한 보호자 (작성자)
     * @param requestList 기록문진 답변 리스트 (questionId + text + childId 포함)
     * @throws IllegalArgumentException 문진 답변 리스트가 비어있거나, 질문을 찾을 수 없는 경우
     */
    @Transactional
    public void submitRecordAnswers(Member member, List<RecordSurveyAnswerDto> requestList) {
        if (requestList == null || requestList.isEmpty()) {
            log.warn("제출된 기록 문진 답변이 비어있습니다.");
            throw new IllegalArgumentException("문진 답변이 비어있습니다.");
        }

        Long childId = requestList.get(0).getChildId();
        log.info("기록 문진 일괄 제출 시작: 자녀 ID={}, 답변 개수={}", childId, requestList.size());
        Child child = childService.findById(childId);

        for (RecordSurveyAnswerDto req : requestList) {
            RecordSurvey survey = recordSurveyRepository.findById(req.getQuestionId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 질문입니다. ID: " + req.getQuestionId()));

            RecordAnswer answer = new RecordAnswer();
            answer.setWriter(member);
            answer.setChild(child);
            answer.setSurvey(survey);
            answer.setQuestion(survey.getQuestion());
            answer.setAnswer(req.getText());
            answer.setAnswered(true);
            repository.save(answer);
        }

        log.info("모든 기록 문진 답변 저장 완료. 포인트 지급 여부 확인 시작.");
        pointService.giveRecordSurveyPointIfEligible(member, child);
        log.info("기록 문진 일괄 제출 및 포인트 지급 로직 완료.");
    }

    /**
     * [관리자용] 특정 부모(Member)에 속한 자녀들의 문진 이력 요약 정보를 가져옵니다.
     * 각 자녀별로 총 문진 기록 날짜 수를 포함합니다.
     *
     * @param memberId 부모(Member) ID
     * @return ChildHistorySummaryDto 리스트
     * @throws EntityNotFoundException 해당 ID의 멤버를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public List<ChildHistorySummaryDto> getChildrenWithHistorySummary(Long memberId) {
        log.info("멤버 ID '{}'의 자녀 문진 이력 요약 조회 요청", memberId);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 멤버가 없습니다. ID: " + memberId));

        List<Child> children = childService.getChildrenByMember(member);

        return children.stream()
                .map(child -> {
                    long totalRecordDatesCount = repository.countDistinctAnswerDatesByChild(child);
                    log.debug("자녀 '{}' (ID: {})의 총 기록 날짜 수: {}", child.getName(), child.getId(), totalRecordDatesCount);
                    return ChildHistorySummaryDto.builder()
                            .childId(child.getId())
                            .childName(child.getName())
                            .childAge(child.getAge())
                            .totalRecordDatesCount(totalRecordDatesCount)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * [관리자용] 특정 자녀의 문진 기록 날짜 목록을 페이징하여 조회합니다.
     *
     * @param childId 자녀 ID
     * @param pageable 페이징 정보 (페이지 번호, 페이지 크기, 정렬)
     * @return RecordDateSummaryDto의 Page 객체
     * @throws EntityNotFoundException 해당 ID의 자녀를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public Page<RecordDateSummaryDto> getRecordDatesPagedByChild(Long childId, Pageable pageable) {
        log.info("자녀 ID '{}'의 기록 날짜 페이징 조회 요청 - 페이지: {}, 사이즈: {}", childId, pageable.getPageNumber(), pageable.getPageSize());
        Child child = childService.findById(childId);

        Page<java.util.Date> distinctSqlDatesPage = repository.findDistinctAnswerDatesByChild(child, pageable);

        return distinctSqlDatesPage.map(sqlDate -> {
            LocalDate localDate = new java.sql.Date(sqlDate.getTime()).toLocalDate();
            return RecordDateSummaryDto.builder()
                    .recordDate(localDate)
                    .childId(child.getId())
                    .build();
        });
    }

    /**
     * [관리자용] 특정 자녀의 특정 날짜에 대한 모든 질문-답변 쌍을 조회합니다.
     *
     * @param childId 자녀 ID
     * @param date 조회할 날짜
     * @return QuestionAnswerPairDto 리스트 (질문-답변 쌍)
     * @throws EntityNotFoundException 해당 ID의 자녀를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public List<QuestionAnswerPairDto> getQuestionsAndAnswersForDate(Long childId, LocalDate date) {
        log.info("자녀 ID '{}'의 '{}' 날짜 기록 문진 상세 조회 요청", childId, date);
        Child child = childService.findById(childId);

        List<RecordAnswer> answers = repository.findByChildAndDate(child, date);

        return answers.stream()
                .map(answer -> new QuestionAnswerPairDto(answer.getQuestion(), answer.getAnswer()))
                .collect(Collectors.toList());
    }
}