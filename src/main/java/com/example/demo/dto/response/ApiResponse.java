package com.example.demo.dto.response;

import lombok.Getter;
import com.example.demo.util.StatusCode;
/**
 * Controller -> Response
 */
@Getter
public class ApiResponse<U> {

    private String name;
    private String code;
    private String message;
    private U data;

    public <T extends Enum<T> & StatusCode> ApiResponse(StatusCode statusCode) {
        this.name = statusCode.Name();
        this.code = statusCode.getCode();
        this.message = statusCode.getMessage();
    }
    public <T extends Enum<T> & StatusCode> ApiResponse(StatusCode statusCode, U data) {
        this.name = statusCode.Name();
        this.code = statusCode.getCode();
        this.message = statusCode.getMessage();
        this.data = data;
    }

}
