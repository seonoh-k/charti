package com.example.demo.users.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "User Is Not Deleted Exception")
public class UserIsNotDeletedException extends RuntimeException{

    public UserIsNotDeletedException() {
        super("활동 상태인 유저 입니다.");
    }

    public UserIsNotDeletedException(String message) {
        super(message);
    }
}
