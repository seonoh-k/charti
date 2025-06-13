package com.example.demo.users.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "MemberNotFoundException")
public class UserNotFoundException extends RuntimeException{

    public UserNotFoundException() {
        super("유저가 없어요");
    }
    public UserNotFoundException(String message) {
        super(message);
    }
}
