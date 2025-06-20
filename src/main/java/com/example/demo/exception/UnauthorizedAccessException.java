package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * {@code UnauthorizedAccessException}
 *
 * 보호자가 자신의 자녀가 아닌 다른 자녀 정보에 접근하려 하거나,
 * 권한이 없는 리소스에 접근을 시도했을 때 발생하는 예외입니다.
 *
 * ✅ 주요 사용 예:
 * - 로그인된 보호자가 다른 사용자의 자녀 정보를 조회 또는 수정하려는 경우
 * - 인증은 되었으나 해당 리소스에 대한 권한이 없을 경우
 *
 * 이 예외는 Spring MVC에서 자동으로 HTTP 403 (Forbidden) 응답을 반환하도록 설정되어 있습니다.
 * (→ {@link ResponseStatus} 어노테이션 활용)
 */
@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Unauthorized Access")
public class UnauthorizedAccessException extends RuntimeException {

    /**
     * 기본 메시지를 포함한 예외 생성자.
     * 메시지: "접근 권한이 없습니다."
     */
    public UnauthorizedAccessException() {
        super("접근 권한이 없습니다.");
    }

    /**
     * 사용자 지정 메시지를 포함한 예외 생성자.
     *
     * @param message 사용자 정의 예외 메시지
     */
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
