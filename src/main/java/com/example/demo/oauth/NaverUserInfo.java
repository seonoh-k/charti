package com.example.demo.oauth;

import java.util.Map;

/**
 * 네이버 소셜 로그인에 대한 유저 정보를 쉽게 파싱 하기위해 생성한 클래스
 * <br/>
 * <br/>
 * Naver Social Login API Response Format <br/>
 * {
 *   "resultcode": "00",
 *   "message": "success",
 *   "response": {
 *     "id": "4269230698",
 *     "nickname": "정성주",
 *     "name": "정성주",
 *     "email": "example@naver.com",
 *     "gender": "M",
 *     "age": "20-29",
 *     "birthday": "03-15",
 *     "birthyear": "1999",
 *     "profile_image": "https://...",
 *     "mobile": "010-1234-5678"
 *   }
 * }
 * 네이버는 "response" 안에 정보 있음
 */
public class NaverUserInfo extends OAuth2UserInfo {

    public NaverUserInfo(Map<String, Object> attributes) {
        super((Map<String, Object>) attributes);
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getProviderId() {
        return (String) ((Map)attributes.get("response")).get("id");
    }

    @Override
    public String getEmail() {
        return (String) ((Map)attributes.get("response")).get("email");
    }

    @Override
    public String getName() {
        return (String) ((Map)attributes.get("response")).get("name");
    }

    @Override
    public String getProviderIdKey() {
        return "response";
    }

}
