package com.example.demo.util;

public enum UserStatus implements StatusCode {
    SOCIAL_LOGIN_SUCCESS("SLS","소셜 로그인 성공"),
    SOCIAL_LOGIN_FAIL("SLF","소셜 로그인 실패"),
    LOGIN_SUCCESS("LS","로그인 성공"),
    LOGIN_FAIL("LF","로그인 실패"),
    JOIN_SUCCESS("JS","회원가입 성공"),
    JOIN_FAIL("JF","회원가입 실패"),
    UPDATE_SUCCESS("US","수정 성공"),
    UPDATE_FAIL("UF","수정 실패"),
    DELETE_SUCCESS("DS","삭제 성공"),
    DELETE_FAIL("DF","삭제 실패");

    private String code;
    private String message;

    UserStatus(String code, String message) {
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
