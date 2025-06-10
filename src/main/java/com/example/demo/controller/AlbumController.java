package com.example.demo.controller;

import com.example.demo.dto.UrlResponse;
import com.example.demo.entity.Album;
import com.example.demo.entity.Photo;
import com.example.demo.service.AlbumService;
import com.example.demo.service.PresignedUrlService;
import com.example.demo.users.entity.Member;
import com.example.demo.users.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AlbumController {

    private final PresignedUrlService urlService;
    private final AlbumService albumService;
    private final MemberService memberService;

    // 앨범 생성
    @PostMapping(value = "/api/album-create", produces = "application/json")
    public ResponseEntity<UrlResponse> createAlbum(@RequestParam("id") Long id,
                                                   @RequestParam("file")MultipartFile file,
                                                   @RequestParam("title") String title) throws IOException {

        String originalFilename = file.getOriginalFilename();
        String mimetype = file.getContentType();
        String filename = UUID.randomUUID() + "_" + originalFilename;

        // 썸네일 업로드용 URL 생성 요청
        URL presignedUrl = urlService.presignedUploadUrl(filename, mimetype);

        // 멤버 조회
        Member member = memberService.get(id);
        // 앨범 생성
        Album album = new Album();
        album.setMember(member);
        album.setTitle(title);
        album.setThumbnail(filename);
        // 데이터 베이스에 앨범 생성
        albumService.create(album);

        // 업로드 URL 반환 -> 썸네일 이미지 업로드
        return ResponseEntity.ok(new UrlResponse(presignedUrl.toString(), filename));
    }

    // 앨범 내 사진 리스트 조회
    @GetMapping("/api/album/{id}")
    public ResponseEntity<List<UrlResponse>> getAlbumDetail(@PathVariable("id") Long id) {
        Album album = albumService.get(id);
        List<Photo> photoList = album.getPhotos();
        List<UrlResponse> urlResponseList = new ArrayList<>();

        for(Photo photo : photoList) {
            URL presignedUrl = urlService.getPresignedUrl(photo.getFileName(), Duration.ofMinutes(15));
            urlResponseList.add(new UrlResponse(presignedUrl.toString(), photo.getFileName()));
        }

        return ResponseEntity.ok(urlResponseList);
    }

}
