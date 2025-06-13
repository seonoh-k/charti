package com.example.demo.util;

public enum AuthStatus implements StatusCode {

    SERVER_ERROR("SE","서버 에러"),
    AUTHENTICATION_FAIL("AF","인증 실패"),
    AUTHENTICATION_SUCCESS("AS","인증 성공"),
    PHONE_AUTH_FAIL("PAF","sms 인증 실패"),
    REGISTRATION_INCOMPLETE("RI","회원가입 미완료");

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
