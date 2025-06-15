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

    public static <T> ApiResponse<T> error(StatusCode statusCode) {
        return new ApiResponse<>(statusCode);
    }

    /**
     * 메세지를 기본 지정된 메세지 외에 다른 메세지를 보여주고 싶은 경우 새로 작성
     * @param statusCode
     * @param overrideMessage
     * @return
     * @param <T>
     */
    public static <T> ApiResponse<T> error(StatusCode statusCode, String overrideMessage) {
        ApiResponse<T> apiResponse = new ApiResponse<>(statusCode);
        apiResponse.message = overrideMessage;
        return apiResponse;
    }

    /**
     * 성공시 + 데이터 반환
     *
     * @param statusCode
     * @param data
     * @return
     * @param <T>
     */
    public static <T> ApiResponse<T> success(StatusCode statusCode, T data) {
        return new ApiResponse<>(statusCode, data);
    }

}
