package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpertUpdateRequest {
    @NotBlank(message = "이름은 필수 입력값입니다.")
    @Pattern(
            regexp = "^[가-힣]{2,8}$",
            message = "이름은 2~8자 한글만 가능합니다. 공백은 불가합니다."
    )
    private String name;
    @Pattern(
            regexp = "^$|^[가-힣A-Za-z0-9_-]{2,8}$",
            message = "닉네임은 2~8자: 한글·영문·숫자·_,- 만 가능합니다. 공백 금지."
    )
    private String nickname;
    private String phoneNumber;
    private Long addressId;
    // 전문가 전용 필드
    private String major;
    private String career;
    private String license;
    private String newUid;
}
