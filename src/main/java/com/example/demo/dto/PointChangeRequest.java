package com.example.demo.dto;

import jakarta.validation.constraints.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointChangeRequest {

    @NotNull(message = "회원 ID는 필수입니다.")
    private Long memberId;

    @Min(value = -100000, message = "포인트는 -100,000 이상이어야 합니다.")
    @Max(value = 100000, message = "포인트는 100,000 이하여야 합니다.")
    private int amount;

    @NotBlank(message = "포인트 변경 사유를 입력하세요.")
    private String description;
}
