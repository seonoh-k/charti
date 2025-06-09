package com.example.demo.controller;

import com.example.demo.dto.UrlResponse;
import com.example.demo.service.PresignedUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class StorageController {

    private final PresignedUrlService urlService;

    @PostMapping("/api/file/upload")
    public ResponseEntity<UrlResponse> uploadFile(
            @RequestParam("file") MultipartFile file) throws IOException {

        String originalFilename = file.getOriginalFilename();
        String mimeType = Files.probeContentType(Paths.get(file.getOriginalFilename()));
        String filename = UUID.randomUUID() + "_" + originalFilename;

        URL presignedUrl = urlService.presignedUploasUrl(filename, mimeType);

        return ResponseEntity.ok(new UrlResponse(presignedUrl.toString(), filename));
    }

    @GetMapping("/api/file/download/{filename}")
    public ResponseEntity<UrlResponse> downloadFile(@PathVariable("filename") String filename) throws IOException {

        URL presignedUrl = urlService.presignedDownloadUrl(filename);

        return ResponseEntity.ok(new UrlResponse(presignedUrl.toString(), filename));
    }

    @GetMapping("/api/file/show/{filename}")
    public ResponseEntity<UrlResponse> showFile(@PathVariable("filename") String filename) throws IOException {

        URL presignedUrl = urlService.getPresignedUrl(filename);

        return ResponseEntity.ok(new UrlResponse(presignedUrl.toString(), filename));
    }
}
