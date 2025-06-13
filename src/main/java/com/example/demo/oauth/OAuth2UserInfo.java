package com.example.demo.oauth;

import java.util.Map;

public abstract class OAuth2UserInfo {

    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public abstract String getProvider();      // google, naver, kakao
    public abstract String getProviderId();    // 구글 id, 네이버 id 등
    public abstract String getEmail(); // 이메일
    public abstract String getName(); // 이름
    public abstract String getProviderIdKey(); // json 응답 객체의 시작점

    public Map<String,Object> getAttributes(){
        return attributes;
    }
}
