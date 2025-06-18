package com.example.demo.util;

import com.example.demo.dto.response.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.stream.Collectors;

// 전역 예외처리 클래스
// -> 동작 중에 발생한 예외는 전부 이 클래스에서 처리된다
// @ExceptionHandler 어노테이션을 사용해 처리할 예외타입을 지정해준다
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // NotValid - 유효성 검사 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidException(MethodArgumentNotValidException e) {
        log.error("ValidException: ", e);
        String message = e.getBindingResult().getFieldErrors()
                .stream()
                .map(ex -> ex.getField() + ": " + ex.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(GlobalStatus.VALIDATION_FAIL, message));
    }

    // 필드 타입이 잘못됨
    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleTypeMismatchException(TypeMismatchException e) {
        log.error("TypeMismatchException: ", e);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(GlobalStatus.TYPE_MISMATCH, e.getMessage()));
    }

    // 리소스가 없음
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleEntityNotFoundException(EntityNotFoundException e) {
        log.error("EntityNotFoundException: ", e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(GlobalStatus.ENTITY_NOT_FOUND,"요청한 데이터를 찾을 수 없습니다."));
    }

    // DB 제약 조건 위반
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("DataIntegrityViolationException: ", e);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(GlobalStatus.DATA_INTEGRITY_VIOLATION, e.getMessage()));
    }

    // 권한 없음
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException e) {
        log.error("AccessDeniedException: ", e);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(GlobalStatus.ACCESS_DENIED));
    }

    // 인증 실패
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthenticationException(AuthenticationException e) {
        log.error("AuthenticationException: ", e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(GlobalStatus.AUTHENTICATION_FAIL, e.getMessage()));
    }

    // JWT 토큰 검증 실패
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<?>> handleJwtException(JwtException e) {
        log.error("JwtException: ", e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(GlobalStatus.JWT_VALIDATION_FAIL, e.getMessage()));
    }

    // Null값 참조
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<?>> handleNullPointerException(NullPointerException e) {
        log.error("NullPointerException: ", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(GlobalStatus.NULL_POINTER, e.getMessage()));
    }

    // 객체 상태 잘못됨
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalStateException(IllegalStateException e) {
        log.error("IllegalStateException: ", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(GlobalStatus.ILLEGAL_STATE, e.getMessage()));
    }

    // 파일, 네트워크 입출력 오류
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiResponse<?>> handleIOException(IOException e) {
        log.error("IOException: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(GlobalStatus.INPUT_OUPUT_ERROR, e.getMessage()));
    }

    // 스토리지 관련 예외처리
    @ExceptionHandler(S3Exception.class)
    public ResponseEntity<ApiResponse<?>> handleAmazonS3Exception(S3Exception e) {
        log.error("S3Exception: ", e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(GlobalStatus.S3_FILE_NOT_FOUND,"파일을 찾을 수 없습니다 : "+ e.getMessage()));
    }

    // 테스트 중에 자꾸 발생해서 일단 예외처리에 추가
    // https://chromium.googlesource.com/devtools/devtools-frontend/+/main/docs/ecosystem/automatic_workspace_folders.md
    // LocalHost Ip나 Localhost로 url이 시작하는 경우
    // DevTools는 자동으로 /.well-known/appspecific/com.chrome.devtools.json 경로를 요청
    @ExceptionHandler(NoResourceFoundException.class)
    public void handleNoResourceFoundException(NoResourceFoundException e) {
        log.info("handleNoResourceFoundException");
        // log.error("NoResourceFoundException: ", e);
    }

    // 작성한 예외 타입과 다른 예외가 발생했을 때 동작
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        log.error("Exception: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(GlobalStatus.UNKNOWN_ERROR));
    }
}
