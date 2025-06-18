package com.example.demo.util;

public enum AuthStatus implements StatusCode {

    SERVER_ERROR("SE","서버 에러"),
    AUTHENTICATION_FAIL("AF","인증 실패"),
    AUTHENTICATION_SUCCESS("AS","인증 성공"),

    PHONE_AUTH_FAIL("PAF","sms 인증 실패"),
    REGISTRATION_INCOMPLETE("RI","회원가입 미완료"),

    EXPERT_JOIN_REQUEST_SUCCESS("EJRS","전문가 회원가입 신청 완료"),
    EXPERT_JOIN_REQUEST_FAIL("EJRF","전문가 회원가입 신청 실패"),

    MANAGER_JOIN_REQUEST_SUCCESS("MJRS","담당자 회원가입 신청 완료"),
    MANAGER_JOIN_REQUEST_FAIL("MJRF","담당자 회원가입 신청 실패"),

    MEMBER_JOIN_REQUEST_SUCCESS("MJRS","일반 회원 회원가입 성공"),
    MEMBER_JOIN_REQUEST_FAIL("MJRF","일반 회원 회원가입 실패"),

    TOKEN_NOT_FOUND("TNF","토큰을 찾을 수 없습니다."),
    TOKEN_INVALID_FORMAT("TIF","토큰이 바른 형식이 아닙니다."),

    USER_NOT_FOUND("UNF","유저를 찾을 수 없어요."),
    USER_NOT_REGISTRATION("UNR","회원가입 절차 미진행"),
    USER_DUPLICATE("UD","중복 이메일 회원가입 불가"),

    ADDRESS_INVALID("AI","유효하지 않은 주소"),

    VALIDATION_FAILED("VF", "유효성 검사 실패"),

    MANAGER_ALREADY_EXISTS("MAE","해당 그룹 담당자가 이미 존재합니다"),

    GROUP_NOT_FOUND("GNF","그룹을 찾을수 없습니다");

    private String code;
    private String message;

    AuthStatus(String code, String message) {
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
