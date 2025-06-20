package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * {@code RecordAnswerNotFoundException}
 *
 * 기록 문진(Record Survey)의 특정 답변(RecordAnswer 엔티티)을 찾을 수 없을 때 발생하는 예외입니다.
 *
 * ✅ 주요 사용 예:
 * - 사용자가 존재하지 않는 답변 ID로 조회/수정/삭제를 시도한 경우
 * - 관리자 또는 보호자가 특정 자녀의 답변을 열람하려 했으나 존재하지 않을 경우
 *
 * 이 예외가 발생하면 클라이언트에는 HTTP 상태 코드 404 (Not Found)가 반환됩니다.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Record Answer Not Found")
public class RecordAnswerNotFoundException extends RuntimeException {

    /**
     * 기본 메시지를 사용하는 생성자.
     * 메시지: "해당 문진 답변을 찾을 수 없습니다."
     */
    public RecordAnswerNotFoundException() {
        super("해당 문진 답변을 찾을 수 없습니다.");
    }

    /**
     * 사용자 정의 메시지를 전달할 수 있는 생성자.
     *
     * @param message 사용자 정의 예외 메시지
     */
    public RecordAnswerNotFoundException(String message) {
        super(message);
    }
}
