package com.example.demo.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManagerUpdateRequestBySuperAdmin {
    private Long id;
    private String name;
    private String nickname;
    private String username;
    private String password;
    private String phoneNumber;
    private String groupName;
    private String groupEmail;
}
