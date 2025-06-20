package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * {@code RecordHistoryNotFoundException}
 *
 * 특정 자녀의 특정 날짜에 기록된 문진 답변 이력이 존재하지 않을 때 발생하는 예외입니다.
 *
 * ✅ 주요 사용 사례:
 * - 관리자가 자녀의 날짜별 문진 이력을 조회하는 경우
 * - 보호자가 마이페이지에서 자녀 문진 기록을 열람할 때
 * - 특정 날짜에 해당하는 데이터가 DB에 존재하지 않음
 *
 * 이 예외는 클라이언트에게 HTTP 404 (Not Found)를 반환합니다.
 * 메시지는 기본 또는 사용자 정의 메시지를 포함할 수 있습니다.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Record History Not Found")
public class RecordHistoryNotFoundException extends RuntimeException {

    /**
     * 기본 메시지를 사용하는 생성자.
     * 메시지: "해당 날짜에 기록된 문진 이력을 찾을 수 없습니다."
     */
    public RecordHistoryNotFoundException() {
        super("해당 날짜에 기록된 문진 이력을 찾을 수 없습니다.");
    }

    /**
     * 사용자 정의 메시지를 전달할 수 있는 생성자.
     *
     * @param message 사용자 정의 예외 메시지
     */
    public RecordHistoryNotFoundException(String message) {
        super(message);
    }

}
