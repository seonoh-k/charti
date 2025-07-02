package com.example.demo.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;


@Data
public class ChildUpdateRequest {
    private Long id;
    @NotBlank(message = "이름은 필수입니다.")
    private String name;
    @NotBlank(message = "태명(닉네임)은 필수입니다.")
    private String nickname;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
    private String gender;
    @NotBlank(message = "키를 입력하세요.")
    private String height;
    @NotBlank(message = "몸무게를 입력하세요.")
    private String weight;
    @NotNull(message = "출생순서를 입력하세요.")
    private Integer birthOrder;
    private Long groupId;



}

