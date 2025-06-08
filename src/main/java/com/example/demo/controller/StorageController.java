package com.example.demo.controller;

import com.example.demo.dto.UploadUrlResponse;
import com.example.demo.entity.Child;
import com.example.demo.service.ChildService;
import com.example.demo.service.PresignedUrlService;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StorageController {

    private final PresignedUrlService urlService;
//    private final ChildService childService;

//    @PostMapping("/api/file/upload")
//    public ResponseEntity<UploadUrlResponse> createUploadUrl(
//            @RequestParam("file")MultipartFile file,
//            @RequestParam("id") Integer id) throws IOException {

//        Child child = childService.get(id);

//        String originalFilename = file.getOriginalFilename();
//        String mimeType = Files.probeContentType(Paths.get(file.getOriginalFilename()));
//        String filename = UUID.randomUUID() + "_" + originalFilename;
//        String uploadFile = "upload/" + child.getNickname() + "/" + filename;

//        URL presignedUrl = urlService.putPresignedUrl(uploadFile, mimeType);

//        childService.updateProfileImage(id, uploadFile);

//        return ResponseEntity.ok(new UploadUrlResponse(presignedUrl.toString(), uploadFile));
//    }

    @GetMapping("api/file/download")
    public ResponseEntity<String> downloadFile(@RequestParam("filename") String filename ) throws IOException {
        return ResponseEntity.ok(urlService.getPresignedUrl(filename).toString());
    }
}
