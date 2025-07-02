package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChildCreateRequest {
    @NotBlank(message = "이름은 필수입니다.")  // 한 글자라도 입력되어야 함
    private String name;

    @NotBlank(message = "태명(닉네임)은 필수입니다.")
    private String nickname; // 태명(닉네임)도 필수로

    @NotBlank(message = "생일은 필수입니다.")
    private String birthday;

    @NotBlank(message = "성별을 선택하세요.")
    private String gender;

    @NotBlank(message = "키를 입력하세요.")
    private String height;

    @NotBlank(message = "몸무게를 입력하세요.")
    private String weight;

    @NotNull(message = "출생순서를 입력하세요.")
    private Integer birthOrder;

    private Long groupId;
}