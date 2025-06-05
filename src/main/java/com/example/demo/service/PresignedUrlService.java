package com.example.demo.service;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PresignedUrlService {

    private static final int EXPIRE_TIME = 60 * 60 * 24;

    private final S3Presigner presigner;

    @Value("${cloudflare.r2.bucket}")
    private String bucketName;

    // 인증된 업로드 URL 생성
    public URL putPresignedUrl(String uploadFilename, String mimeType) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName).key(uploadFilename)
                .contentType(mimeType).build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(objectRequest)
                .build();

        return presigner.presignPutObject(presignRequest).url();
    }

    // 인증된 다운로드 URL 생성
    public URL getPresignedUrl(String filename) {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucketName).key(filename).build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(EXPIRE_TIME))
                .getObjectRequest(objectRequest)
                .build();

        return presigner.presignGetObject(presignRequest).url();
    }

}
