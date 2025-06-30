package com.example.demo.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpertUpdateRequest {
    private String name;
    private String nickname;
    private String phoneNumber;
    private Long addressId;
    // 전문가 전용 필드
    private String major;
    private String career;
    private String license;
    private String newUid;
}
