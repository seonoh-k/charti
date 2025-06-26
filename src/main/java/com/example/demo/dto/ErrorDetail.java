package com.example.demo.dto;

import lombok.Data;

@Data
public class ErrorDetail {

    private String field;
    private String message;

    public ErrorDetail(String field, String message) {
        this.field = field;
        this.message = message;
    }

}