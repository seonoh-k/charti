package com.example.demo.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginAttemptRequest {
    private String username;
    private Boolean success;
    private String failureReason;
}
