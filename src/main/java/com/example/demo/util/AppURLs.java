package com.example.demo.util;

import lombok.Getter;

import java.util.Arrays;

/**
 * 인증 및 권한별 허용/차단 URL 상수 정의
 */
@Getter
public class AppURLs {

    /**
     * Security, JwtAuthenticationFilter에서 사용
     */
    public static final String[] PUBLIC_URLS = {
            // TODO: 인증 자체가 필요 없는 url을 정확하게 작성
            "/loginForm",
            "/joinForm",
            "/oauth2/login",
            "/join",
            "/login",
            "/expertJoinForm",
            "/managerJoinForm",
            "/joinExpert",
            "/joinManager",
            "/api/check-phone",
            "/favicon.ico"
    };

    /**
     * 정적 리소스(prefix) 화이트리스트 URL
     *
     * Security, JwtAuthenticationFilter에서 사용
     */
    public static final String[] PREFIX_WHITELIST = {
            // TODO: 인증 자체가 필요 없는 url 접두사를 작성
            "/css/**",
            "/js/**",
            "/images/**",
            "/.well-known/**",
            "/test/**",
            "/join/**"

    };

    /**
     * 나머지 : SecuirtyConfig에서 사용
     */
    public static final String[] MEMBER_URLS = {
            // TODO: Member 전용 엔드포인트 명시
    };
    public static final String[] MANAGER_URLS = {
            // TODO: Manager 전용 엔드포인트 명시
    };
    public static final String[] EXPERT_URLS = {
            // TODO: Expert 전용 엔드포인트 명시
    };
    public static final String[] ADMIN_URLS = {
            // TODO: Admin 전용 엔드포인트 명시
    };


    public static final String[] MEMBER_URLS_PREFIX = {
            // TODO: Member 전용 PREFIX 작성
            "/member/**"
    };
    public static final String[] ADMIN_URLS_PREFIX = {
            // TODO: Admin 전용 PREFIX 작성
            "/admin/**"
    };
    public static final String[] MANAGER_URLS_PREFIX = {
            // TODO: Manager 전용 PREFIX 작성
            "/manager/**"
    };
    public static final String[] EXPERT_URLS_PREFIX = {
            // TODO: Expert 전용 PREFIX 작성
            "/expert/**"
    };


    /** 2중 권한 조합: Member + Manager */
    public static final String[] MEMBER_MANAGER_URLS = {
            // TODO: Member, Manager 공통 엔드포인트 명시, 예: "/manager/fuser"
    };
    /** 2중 권한 조합: Member + Expert */
    public static final String[] MEMBER_EXPERT_URLS = {
            // TODO: Member, Expert 공통 엔드포인트 명시
    };
    /** 2중 권한 조합: Admin + Manager */
    public static final String[] ADMIN_MANAGER_URLS = {
            // TODO: Admin, Manager 공통 엔드포인트 명시
    };
    /** 2중 권한 조합: Admin + Expert */
    public static final String[] ADMIN_EXPERT_URLS = {
            // TODO: Admin, Expert 공통 엔드포인트 명시
    };
    /** 2중 권한 조합: Manager + Expert */
    public static final String[] MANAGER_EXPERT_URLS = {
            // TODO: Manager, Expert 공통 엔드포인트 명시
    };


    /** 3중 권한 조합: Member + Admin + Expert */
    public static final String[] MEMBER_ADMIN_EXPERT_URLS = {
            // TODO: Member, Admin, Expert 공통 엔드포인트 명시
    };
    /** 3중 권한 조합: Member + Manager + Expert */
    public static final String[] MEMBER_MANAGER_EXPERT_URLS = {
            // TODO: Member, Manager, Expert 공통 엔드포인트 명시
    };
    /** 3중 권한 조합: Admin + Manager + Expert */
    public static final String[] ADMIN_MANAGER_EXPERT_URLS = {
            // TODO: Admin, Manager, Expert 공통 엔드포인트 명시
    };

    public static String[] getCombineURL(String[]... arrays) {
        return Arrays.stream(arrays)
                .flatMap(Arrays::stream)
                .toArray(String[]::new);
    }

}

