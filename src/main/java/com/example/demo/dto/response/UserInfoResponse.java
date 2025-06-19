package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoResponse {
//    내정보 요청시  그냥 userDTO로 써도 되는건가
    private String name;
    private String email;
    private String nickname;
    private String phoneNumber;
//    private String profileImage;

}