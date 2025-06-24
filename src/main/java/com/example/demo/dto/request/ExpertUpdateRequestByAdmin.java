package com.example.demo.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpertUpdateRequestByAdmin {
    private Long id;
    private String name;
    private String nickname;
    private String username;
    private String phoneNumber;
    private String major;
    private String career;
}
