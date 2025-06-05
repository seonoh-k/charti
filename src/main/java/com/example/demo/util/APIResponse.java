package com.example.demo.util;

import lombok.AllArgsConstructor;

// API 응답 모듈 - RestController에 작성할 API의 응답을 일관된 방식으로 작성하기 위해 모듈 작성
// 사용 방법
// RestController에 메소드 작성 시 사용
// public ResponseEntity<APIResponse<Child>> getChild(Integer id)
// 성공 시 반환값 - return ResponseEntity<APIResponse.ok(Child)>
// 실패 시 반환값 - return ResponseEntity<APIResponse.fail("에러 메세지 입력")>
@AllArgsConstructor
public class APIResponse<T> {
    private boolean success;
    private T data;
    private String message;

    public static <T> APIResponse<T> ok(T data) {
        return new APIResponse<>(true, data, null);
    }

    public static <T> APIResponse<T> error(String message) {
        return new APIResponse<>(false, null, message);
    }
}
