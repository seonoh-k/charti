package com.example.demo.dto.request;

import lombok.Data;
import lombok.Setter;


@Data
public class AdminLoginRequest {
    private String username;
    private String password;
}
