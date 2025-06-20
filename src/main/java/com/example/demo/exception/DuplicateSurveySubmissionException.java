package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * {@code DuplicateSurveySubmissionException}
 *
 * 사용자가 하루에 한 번만 제출할 수 있는 문진(예: 기록 문진)을 중복하여 제출하려고 할 때 발생하는 예외입니다.
 *
 * ✅ 주요 사용 예:
 * - 특정 자녀에 대해 동일한 날짜에 동일 유형의 문진을 다시 제출하려는 경우
 * - 서버 측에서 중복 제출 여부를 감지하고 클라이언트에 Conflict(409) 상태로 응답하기 위해 사용됩니다.
 *
 * 이 예외는 Spring MVC에서 자동으로 HTTP 409 (Conflict) 응답을 반환하도록 설정되어 있습니다.
 * (→ {@link ResponseStatus} 어노테이션 활용)

 */
@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Duplicate Survey Submission")
public class DuplicateSurveySubmissionException extends RuntimeException {

    /**
     * 기본 메시지를 포함한 예외 생성자.
     * 메시지: "이미 오늘 문진을 제출했습니다."
     */
    public DuplicateSurveySubmissionException() {
        super("이미 오늘 문진을 제출했습니다.");
    }

    /**
     * 사용자 지정 메시지를 포함한 예외 생성자.
     *
     * @param message 사용자 정의 예외 메시지
     */
    public DuplicateSurveySubmissionException(String message) {
        super(message);
    }
}
