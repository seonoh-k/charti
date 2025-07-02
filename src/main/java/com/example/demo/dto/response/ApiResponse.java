package com.example.demo.dto.response;

import com.example.demo.dto.ErrorDetail;
import lombok.Getter;
import com.example.demo.util.StatusCode;

import java.util.List;

/**
 * Controller -> Response
 */
@Getter
public class ApiResponse<U> {

    private String name;
    private String code;
    private String message;
    private U data;
    private String fieldName;   // 유효성검사를 위한필드네임

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

    public <T extends Enum<T> & StatusCode> ApiResponse(StatusCode statusCode, String fieldName, U data) {
        this.name    = statusCode.Name();
        this.code    = statusCode.getCode();
        this.message = statusCode.getMessage();
        this.fieldName = fieldName;
        this.data    = data;
    }
    // ✨ 새로 추가될 메서드를 여기에 넣으시면 됩니다.
    public static <T, V extends Enum<V> & StatusCode> ApiResponse<T> success(V statusCode, String message, T data) {
        ApiResponse<T> apiResponse = new ApiResponse<>(statusCode, data);
        apiResponse.message = message; // 기본 메시지를 오버라이드
        return apiResponse;
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
    public static <T extends List<ErrorDetail>> ApiResponse<T> responseError(StatusCode statusCode, T data) {
        return new ApiResponse<>(statusCode, data);
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
    public static <T> ApiResponse<T> success(StatusCode statusCode, String overrideMessage) {
        ApiResponse<T> apiResponse = new ApiResponse<>(statusCode);
        apiResponse.message = overrideMessage;
        return apiResponse;
    }

}
