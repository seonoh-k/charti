package com.example.demo.survey.dto;

import com.example.demo.survey.entity.RecordAnswer;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 기록 문진 답변 정보를 클라이언트에게 반환할 때 사용되는 DTO입니다.
 * 단일 답변에 대한 상세 정보를 포함합니다.
 */
@Getter
@AllArgsConstructor
public class RecordAnswerResponse {
    /**
     * 답변의 고유 ID입니다.
     */
    private Long answerId;
    /**
     * 답변된 문진 질문의 내용입니다. (예: "오늘 하루 기분은 어떠했나요?")
     */
    private String question;
    /**
     * 사용자가 입력한 답변 내용입니다.
     */
    private String answer;
    /**
     * 답변을 작성한 자녀의 이름입니다.
     */
    private String childName;
    /**
     * 답변이 생성된 일시입니다.
     */
    private LocalDateTime createdAt;

    /**
     * RecordAnswer 엔티티를 RecordAnswerResponse DTO로 변환합니다.
     * 이 메서드는 서비스 계층에서 엔티티를 조회한 후 클라이언트에게 응답하기 전에 데이터를 가공할 때 사용됩니다.
     *
     * @param entity 변환할 RecordAnswer 엔티티
     * @return RecordAnswerResponse DTO
     */
    public static RecordAnswerResponse fromEntity(RecordAnswer entity) {
        return new RecordAnswerResponse(
                entity.getAnswerId(),
                entity.getSurvey().getQuestion(), // RecordAnswer는 RecordSurvey 엔티티에 대한 참조를 가집니다.
                entity.getAnswer(),
                entity.getChild().getName(),      // RecordAnswer는 Child 엔티티에 대한 참조를 가집니다.
                entity.getCreatedAt()
        );
    }
}