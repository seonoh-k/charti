package com.example.demo.users.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Admin Already Exists Exception")
public class AdminAlreadyExistsException extends RuntimeException{

    public AdminAlreadyExistsException() {
        super("사용할 수 없는 이메일이에요");
    }

    public AdminAlreadyExistsException(String message) {
        super(message);
    }

}
