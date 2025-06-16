package com.example.demo.users.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;

/**
 * GlobalExceptionHandler Test Controller 추후 삭제
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class TestController {

    // 입력값 검증 실패
    @GetMapping("/test/validation")
    public void testValidation() throws MethodArgumentNotValidException {
        // known으로 발생
        throw new MethodArgumentNotValidException(null, null);
    }

    // 입력 타입 불일치
    @GetMapping("/test/type-mismatch")
    public void testTypeMismatch() {
        // TYPE_MISMATCH
        throw new TypeMismatchException("abc", Integer.class);
    }

    // 리소스 부재
    @GetMapping("/test/not-found")
    public void testEntityNotFound() {
        // ENTITY_NOT_FOUND
        throw new EntityNotFoundException("해당 엔티티가 없습니다");
    }

    // 데이터 제약 조건 위반
    @GetMapping("/test/data-integrity")
    public void testDataIntegrity() {
        // DATA_INTEGRITY_VIOLATION
        throw new DataIntegrityViolationException("유니크 제약 조건 위반");
    }

    // 권한이 없습니다
    @GetMapping("/test/access-denied")
    public void testAccessDenied() {
        // ACCESS_DENIED
        throw new AccessDeniedException("접근 불가");
    }

    // 인증 실패
    @GetMapping("/test/auth-fail")
    public void testAuthFail() {
        // AUTHENTICATION_FAIL
        throw new AuthenticationException("인증 정보 없음") {};
    }

    // JWT 검증 실패
    @GetMapping("/test/jwt-fail")
    public void testJwtFail() {
        // JWT_VALIDATION_FAIL
        throw new JwtException("잘못된 JWT");
    }

    // Null 참조 예외
    @GetMapping("/test/null-pointer")
    public void testNullPointer() {
        // NULL_POINTER
        throw new NullPointerException("널 참조");
    }

    // 잘못된 상태 예외
    @GetMapping("/test/illegal-state")
    public void testIllegalState() {
        // ILLEGAL_STATE
        throw new IllegalStateException("잘못된 상태");
    }

    // 입출력 오류
    @GetMapping("/test/io-error")
    public void testIoError() throws IOException {
        // INPUT_OUPUT_ERROR
        throw new IOException("I/O 오류 발생");
    }

    // 스토리지 파일을 찾을 수 없습니다
    @GetMapping("/test/s3-error")
    public void testS3Error() {
        // S3_FILE_NOT_FOUND
        throw S3Exception.builder().message("S3에서 파일을 찾을 수 없음").build();
    }

    // NoResourceFoundException 은 void 처리 (로그만)
    @GetMapping("/test/no-resource")
    public void testNoResource() throws NoResourceFoundException {
        // STATIC_RESOURCE_NOT_FOUND
        throw new NoResourceFoundException(HttpMethod.GET,"/test/no-resource");
    }

    // 알 수 없는 오류
    @GetMapping("/test/unknown-error")
    public void testUnknownError() {
        // UNKNOWN_ERROR
        throw new RuntimeException("예상치 못한 예외");
    }
}
