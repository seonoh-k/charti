package com.example.demo.survey.dto;

import com.example.demo.survey.entity.RecordAnswer;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 기록 문진 답변 정보를 응답용으로 변환하는 DTO.
 * 주로 사용자 또는 관리자 페이지에서 답변 이력을 표시할 때 사용됩니다.
 */
@Getter
public class RecordAnswerResponse {

    /**
     * 답변 ID (기본 키).
     */
    private Long answerId;

    /**
     * 연결된 질문 ID (RecordSurvey의 surveyId).
     */
    private Long questionId;

    /**
     * 질문 내용.
     */
    private String question;

    /**
     * 사용자 작성 답변.
     */
    private String answer;

    /**
     * 자녀 이름.
     */
    private String childName;

    /**
     * 답변 작성 시각.
     */
    private LocalDateTime createdAt;

    /**
     * 모든 필드를 포함하는 명시적 생성자.
     *
     * @param answerId   답변 ID
     * @param questionId 질문 ID
     * @param question   질문 내용
     * @param answer     작성한 답변
     * @param childName  자녀 이름
     * @param createdAt  작성 일시
     */
    public RecordAnswerResponse(Long answerId, Long questionId, String question, String answer, String childName, LocalDateTime createdAt) {
        this.answerId = answerId;
        this.questionId = questionId;
        this.question = question;
        this.answer = answer;
        this.childName = childName;
        this.createdAt = createdAt;
    }

    /**
     * RecordAnswer 엔티티를 DTO로 변환하는 정적 메서드.
     *
     * @param entity RecordAnswer 엔티티
     * @return 변환된 RecordAnswerResponse DTO
     */
    public static RecordAnswerResponse fromEntity(RecordAnswer entity) {
        return new RecordAnswerResponse(
                entity.getAnswerId(),
                entity.getSurvey().getSurveyId(),
                entity.getQuestion(),
                entity.getAnswer(),
                entity.getChild().getName(),
                entity.getCreatedAt()
        );
    }

    // 일반 회원의 마이페이지에서 기록 문진 이력을 간략히 조회
    private String created;
    public RecordAnswerResponse (RecordAnswer answer) {
        this.answerId = answer.getAnswerId();
        this.childName = answer.getChild().getName();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        this.created = answer.getCreatedAt().format(formatter);
    }
}