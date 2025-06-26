package com.example.demo.users.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Admin Not Found Exception")
public class AdminNotFoundException extends RuntimeException{

    public AdminNotFoundException() {
        super("해당 하는 관리자가 없어요.");
    }
    public AdminNotFoundException(String message) {
        super(message);
    }
}