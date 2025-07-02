package com.example.demo.users.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "UserAlreadyExistsException")
public class UserAlreadyExistsException extends RuntimeException{

    public UserAlreadyExistsException() {
        super("사용할 수 없는 이메일이에요");
    }
    public UserAlreadyExistsException(String message) {
        super(message);
    }

}
