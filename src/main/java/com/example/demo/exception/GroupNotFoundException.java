package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Group Not Found Exception")
public class GroupNotFoundException extends RuntimeException{

    public GroupNotFoundException() {
        super("해당 자녀를 찾을 수 없습니다.");
    }

    public GroupNotFoundException(String message) {
        super(message);
    }

}
