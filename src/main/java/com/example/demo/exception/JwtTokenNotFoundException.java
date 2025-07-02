package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Default Message -> "토큰을 찾을 수 없습니다."
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Jwt Token Not Found Exception")
public class JwtTokenNotFoundException extends RuntimeException{

    public JwtTokenNotFoundException() {
        super("토큰을 찾을 수 없습니다.");
    }

    public JwtTokenNotFoundException(String message) {
        super(message);
    }

}
