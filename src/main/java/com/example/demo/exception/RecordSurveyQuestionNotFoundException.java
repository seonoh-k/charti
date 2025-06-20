package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * {@code RecordSurveyQuestionNotFoundException}
 *
 * 기록 문진(Record Survey) 처리 중 특정 질문(RecordSurvey 엔티티)을 찾을 수 없는 경우 발생하는 예외입니다.
 *
 * ✅ 주요 사용 예:
 * - 클라이언트가 제출한 질문 ID가 존재하지 않거나 삭제된 경우
 * - 관리자 또는 사용자가 존재하지 않는 질문에 접근하려고 할 때
 *
 * 이 예외는 Spring MVC에서 자동으로 HTTP 404 (Not Found) 상태로 응답되도록 지정되어 있습니다.
 * (→ {@link ResponseStatus} 어노테이션 사용)
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Record Survey Question Not Found")
public class RecordSurveyQuestionNotFoundException extends RuntimeException {

    /**
     * 기본 메시지를 포함한 예외 생성자.
     * 메시지: "해당 문진 질문을 찾을 수 없습니다."
     */
    public RecordSurveyQuestionNotFoundException() {
        super("해당 문진 질문을 찾을 수 없습니다.");
    }

    /**
     * 사용자 지정 메시지를 포함한 예외 생성자.
     *
     * @param message 사용자 정의 예외 메시지
     */
    public RecordSurveyQuestionNotFoundException(String message) {
        super(message);
    }
}
