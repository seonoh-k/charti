package com.example.demo.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserDTO {

    private Long id;
    private String uuid;
    private String name;
    private String username;
    private String password;
    private String role;
    private String provider;
    private String providerId;
    private String smsIdToken; // sms 인증
    private String phoneNumber;


}
