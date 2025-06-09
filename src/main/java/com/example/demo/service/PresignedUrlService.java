package com.example.demo.service;


import java.net.URL;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class PresignedUrlService {

    // 인증된(보호받는) URL 생성 클래스
    // PresignedUrl을 사용하는 이유
    // - R2스토리지는 기본 프라이빗 설정. 퍼블릭의 경우 파일명만 알면 데이터 출이 가능함.
    // - PresignedUrl을 사용하면 스토리지는 프라이빗인 상태로 접근이 가능.
    // - 유효 기간의 경우 업로드/다운로드는 짧은 시간으로 설정, 프로필 이미지 출력용으로는 24시간 설정.
    // - 유효 기간을 너무 길게 설정하는 것도 보안 상의 이유로 추천되지 않음. 유효 기간이 지나면 url은 사라짐.

    // URL 유효 기간 설정 = 24시간
    private static final int EXPIRE_TIME = 60 * 60 * 24;

    // S3 인증 클래스
    private final S3Presigner presigner;

    // R2 버킷 이름
    @Value("${cloudflare.r2.bucket}")
    private String bucketName;

    // 업로드 URL 생성
    public URL presignedUploasUrl(String uploadFilename, String mimeType) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName) // R2 스토리지 버킷 이름
                .key(uploadFilename) // 업로드할 파일 이름
                .contentType(mimeType) // 업로드할 파일의 확장자
                .build();

        PutObjectPresignRequest presignedRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15)) // 유효 기간을 15분으로 설정
                .putObjectRequest(objectRequest).build();

        // 인증된 url을 반환
        return presigner.presignPutObject(presignedRequest).url();
    }

    // 다운로드 URL 생성
    public URL presignedDownloadUrl(String filename) {

        // 업로드 할때 붙여준 UUID 잘라내기
        int index = filename.lastIndexOf("_");
        String originalFilename = filename.substring(index + 1);

        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucketName) // R2 스토리지 버킷 이름
                .key(filename) // 다운로드할 파일 이름
                // 다운로드 할 파일 이름을 UUID를 잘라낸 이름으로 지정
                .responseContentDisposition("attachment; filename=\"" + originalFilename + "\"")
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15)) // 유효 기간을 15분으로 설정
                .getObjectRequest(objectRequest).build();

        return presigner.presignGetObject(presignRequest).url();
    }

    // 이미지 조회 URL 생성
    public URL getPresignedUrl(String filename) {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucketName) // R2 스토리지 버킷 이름
                .key(filename) // 다운로드할 파일 이름
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(EXPIRE_TIME)) // 유효 기간을 24시간으로 설정
                .getObjectRequest(objectRequest).build();

        return presigner.presignGetObject(presignRequest).url();
    }
}
