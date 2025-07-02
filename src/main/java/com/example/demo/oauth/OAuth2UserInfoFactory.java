package com.example.demo.oauth;

import java.util.Map;

public interface OAuth2UserInfoFactory {

    /**
     * 해당하는 registrationId가 우리가 제공하는 소셜 로그인 서비스 인지 확인.
     * @param registrationId "google", "kakao", "naver"
     * @return true - 지원 false - 미지원
     */
    boolean supports(String registrationId);

    /**
     * Map기반의 attributes로 OAuth2UserInfo 객체를 생성한다.
     *
     * @param attributes
     * @return OAuth2UserInfo 실제로는 각 서비스의 XxxXUserInfo 객체를 반환
     */
    OAuth2UserInfo create(Map<String, Object> attributes);
}