package com.example.demo.users.exception;

public class AddressAlreadyInUseException extends RuntimeException {
    public AddressAlreadyInUseException() {
        super("이 주소는 사용할 수 없습니다");
    }
    public AddressAlreadyInUseException(String message) {
        super(message);
    }
}
