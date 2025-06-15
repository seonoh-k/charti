package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Address Not Found Exception")
public class AddressNotFoundException extends RuntimeException{

    public AddressNotFoundException() {
        super("주소를 찾을 수 없어요.");
    }

    public AddressNotFoundException(String message) {
        super(message);
    }
}
