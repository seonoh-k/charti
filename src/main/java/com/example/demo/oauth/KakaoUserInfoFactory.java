package com.example.demo.oauth;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KakaoUserInfoFactory implements OAuth2UserInfoFactory {
    @Override
    public boolean supports(String registrationId) {
        return "kakao".equals(registrationId);
    }

    @Override
    public OAuth2UserInfo create(Map<String, Object> attributes) {
        return new KakaoUserInfo(attributes);
    }
}

