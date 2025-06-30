package com.example.demo.survey.dto;

import lombok.Data;

/**
 * 기록 문진의 여러 답변을 일괄적으로 처리하거나,
 * 특히 프론트엔드에서 특정 문진 질문에 대한 답변을 제출할 때 사용되는 DTO입니다.
 * 주로 질문 ID, 답변 텍스트, 자녀 ID를 묶어서 전달합니다.
 *
 * <p>참고: {@link RecordAnswerRequest}가 단일 답변 요청에 사용된다면,
 * 이 DTO는 여러 답변을 리스트 형태로 묶어서 보낼 때 적합합니다.</p>
 */
@Data // @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor를 포함
public class RecordSurveyAnswerDto {
    /**
     * 답변하고자 하는 문진 질문의 고유 ID입니다.
     */
    private Long questionId;
    /**
     * 사용자가 작성한 주관식 답변 내용입니다.
     */
    private String text;
    /**
     * 이 답변이 속한 자녀의 고유 ID입니다.
     */
    private Long childId;

}