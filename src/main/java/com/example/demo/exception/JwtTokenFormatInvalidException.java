package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Default message -> "잘못된 형식의 토큰"
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Jwt Token Format Invalid Exception")
public class JwtTokenFormatInvalidException extends RuntimeException{

    public JwtTokenFormatInvalidException() {
        super("잘못된 형식의 토큰");
    }

    public JwtTokenFormatInvalidException(String message) {
        super(message);
    }

}
