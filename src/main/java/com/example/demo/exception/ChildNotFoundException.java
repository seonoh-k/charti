package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Child Not Found Exception")
public class ChildNotFoundException extends RuntimeException {

    public ChildNotFoundException() {
        super("해당 자녀를 찾을 수 없습니다.");
    }

    public ChildNotFoundException(String message) {
        super(message);
    }

}