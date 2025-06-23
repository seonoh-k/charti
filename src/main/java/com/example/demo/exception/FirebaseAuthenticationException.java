package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY, reason = "Firebase Authentication Exception")
public class FirebaseAuthenticationException extends RuntimeException{
    public FirebaseAuthenticationException() {
        super("외부 인증 서버에 계정이 없어요.");
    }

    public FirebaseAuthenticationException(String message) {
        super(message);
    }
}
