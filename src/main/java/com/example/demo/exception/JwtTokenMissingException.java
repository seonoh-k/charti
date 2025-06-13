package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "JwtTokenMissingException")
public class JwtTokenMissingException extends RuntimeException {
    public JwtTokenMissingException() {
        super("토큰이 없어요!");
    }

    public JwtTokenMissingException(String message) {
        super(message);
    }
}