package com.example.demo.dto.request;

import com.example.demo.enums.TargetGroup;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManagerUpdateRequest {
    private String name;
    private String nickname;
    private String phoneNumber;
    private Long addressId; // 그룹주소
    // 담당자 전용 필드
    private String groupName;
    private String groupEmail;
    private String groupPhoneNumber;
    private String targetGroup;
    private String newUid;

}