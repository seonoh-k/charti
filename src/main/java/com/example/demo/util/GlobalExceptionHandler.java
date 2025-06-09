package com.example.demo.util;

//import com.amazonaws.services.s3.model.AmazonS3Exception;
import groovy.util.logging.Slf4j;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.io.IOException;
import java.util.stream.Collectors;

// 전역 예외처리 클래스
// -> 동작 중에 발생한 예외는 전부 이 클래스에서 처리된다
// @ExceptionHandler 어노테이션을 사용해 처리할 예외타입을 지정해준다
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // NotValid - 유효성 검사 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse<?>> handleValidException(MethodArgumentNotValidException e) {
        logger.error(e.getMessage());
        String message = e.getBindingResult().getFieldErrors()
                .stream()
                .map(ex -> ex.getField() + ": " + ex.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(APIResponse.error("입력값 오류 : " + message));
    }

    // 필드 타입이 잘못됨
    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<APIResponse<?>> handleTypeMismatchException(TypeMismatchException e) {
        logger.error(e.getMessage());
        return ResponseEntity.badRequest()
                .body(APIResponse.error("입력 타입 다름 : " + e.getMessage()));
    }

    // 리소스가 없음
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<APIResponse<?>> handleEntityNotFoundException(EntityNotFoundException e) {
        logger.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(APIResponse.error("요청한 데이터를 찾을 수 없습니다."));
    }

    // DB 제약 조건 위반
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<APIResponse<?>> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        logger.error(e.getMessage());
        return ResponseEntity.badRequest()
                .body(APIResponse.error("제약 조건 위반 : " + e.getMessage()));
    }

    // 권한 없음
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<APIResponse<?>> handleAccessDeniedException(AccessDeniedException e) {
        logger.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(APIResponse.error("권한이 없습니다."));
    }

    // 인증 실패
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<APIResponse<?>> handleAuthenticationException(AuthenticationException e) {
        logger.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(APIResponse.error("인증 실패 : " + e.getMessage()));
    }

    // JWT 토큰 검증 실패
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<APIResponse<?>> handleJwtException(JwtException e) {
        logger.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(APIResponse.error("JWT 토큰 검증 실패 : " + e.getMessage()));
    }

    // Null값 참조
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<APIResponse<?>> handleNullPointerException(NullPointerException e) {
        logger.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(APIResponse.error(e.getMessage()));
    }

    // 객체 상태 잘못됨
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<APIResponse<?>> handleIllegalStateException(IllegalStateException e) {
        logger.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(APIResponse.error(e.getMessage()));
    }

    // 파일, 네트워크 입출력 오류
    @ExceptionHandler(IOException.class)
    public ResponseEntity<APIResponse<?>> handleIOException(IOException e) {
        logger.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(APIResponse.error(e.getMessage()));
    }

//    @ExceptionHandler(AmazonS3Exception.class)
//    public ResponseEntity<APIResponse<?>> handleAmazonS3Exception(AmazonS3Exception e) {
//        logger.error(e.getMessage());
//        return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                .body(APIResponse.error("파일을 찾을 수 없습니다 : " + e.getMessage()));
//    }

    // 작성한 예외 타입과 다른 예외가 발생했을 때 동작
    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<?>> handleException(Exception e) {
        logger.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(APIResponse.error(e.getMessage()));
    }
}
