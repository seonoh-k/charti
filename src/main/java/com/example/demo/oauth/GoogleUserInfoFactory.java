package com.example.demo.oauth;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GoogleUserInfoFactory implements OAuth2UserInfoFactory {
    @Override
    public boolean supports(String registrationId) {
        return "google".equals(registrationId);
    }

    @Override
    public OAuth2UserInfo create(Map<String, Object> attributes) {
        return new GoogleUserInfo(attributes);
    }
}

