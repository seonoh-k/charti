package com.example.demo.util;

public enum GlobalStatus implements StatusCode{

    VALIDATION_FAIL("VF", "입력값 검증 실패"),
    TYPE_MISMATCH("TM", "입력 타입 불일치"),

    ENTITY_NOT_FOUND("ENF", "요청한 데이터를 찾을 수 없습니다"),
    DATA_INTEGRITY_VIOLATION("DIV", "데이터 제약 조건 위반"),

    ACCESS_DENIED("AD", "권한이 없습니다"),
    AUTHENTICATION_FAIL("AF", "인증 실패"),
    JWT_VALIDATION_FAIL("JVF", "JWT 검증 실패"),

    NULL_POINTER("NP", "Null 참조 예외"),

    ILLEGAL_STATE("IS", "잘못된 상태 예외"),

    INPUT_OUPUT_ERROR("IOE", "입출력 오류"),

    S3_FILE_NOT_FOUND("SFNF", "스토리지 파일 미발견"),

    UNKNOWN_ERROR("NE", "알 수 없는 오류"),
    STATIC_RESOURCE_NOT_FOUND("SRNF","정적 리소스 탐색 실패");


    private String code;
    private String message;

    GlobalStatus(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return this.code;
    }
    @Override
    public String getMessage() {
        return this.message;
    }
}
