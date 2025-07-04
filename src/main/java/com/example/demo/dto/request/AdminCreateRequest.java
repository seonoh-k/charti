package com.example.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminCreateRequest {

    private Long id;
    // 공통
    @Pattern(regexp = "^[가-힣]{2,8}$", message = "이름은 한글만 사용할 수 있어요.")
    @NotBlank(message = "이름은 필수 입력 값이에요.")
    private String name;

    @NotBlank(message = "담당 업무도 필수로 입력해주세요")
    private String position;
    private String uuid;


    @Email(message = "올바른 이메일 형식이 아니에요.")
    @NotBlank(message = "아이디는 필수 입력 값이에요.")
    private String username;


    @Size(min = 6, max = 20, message = "비밀번호는 6자 이상 20자 이하여야 합니다.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#$%^&*(),.?\":{}|<>]{6,20}$",
            message = "비밀번호는 소문자, 대문자, 숫자, 특수문자를 각각 최소 1자 이상 포함해야해요."
    )
    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    private String password;

    @NotBlank(message = "전화번호는 필수 입력값입니다.")
    private String phoneNumber;

    // 주소
}
