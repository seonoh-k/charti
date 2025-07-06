package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManagerUpdateRequestByAdmin {

    private Long id;

    @Pattern(regexp = "^[가-힣]{2,8}$", message = "이름은 한글만 사용할 수 있어요.")
    @NotBlank(message = "이름은 필수 입력 값이에요.")
    private String name;

    @NotBlank(message = "닉네임은 필수 입력 값이에요.")
    @Pattern(
            regexp = "^[가-힣A-Za-z0-9_-]{2,8}$",
            message = "닉네임은 2~8자, 한글·영문·숫자·_,- 만 가능해요.(공백은 불가)"
    )
    private String nickname;


}
