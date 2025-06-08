package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UploadUrlResponse {
    // 파일 업로드 URL 정보를 담는 DTO 클래스
    private String url;
    private String fileName;
}
