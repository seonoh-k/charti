package com.example.demo.controller;

import com.example.demo.dto.UrlResponse;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.entity.Album;
import com.example.demo.entity.Photo;
import com.example.demo.service.AlbumService;
import com.example.demo.service.PhotoService;
import com.example.demo.service.PresignedUrlService;
import com.example.demo.users.entity.Member;
import com.example.demo.users.service.MemberService;
import com.example.demo.util.APIResponse;
import com.example.demo.util.GlobalStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StorageAPIController {

    private final PresignedUrlService urlService;
    private final AlbumService albumService;
    private final PhotoService photoService;
    private final MemberService memberService;

    @PostMapping("/api/file/upload")
    public ResponseEntity<UrlResponse> uploadFile(
            @RequestParam("file") MultipartFile file) throws IOException {

        String originalFilename = file.getOriginalFilename();
        String mimeType = Files.probeContentType(Paths.get(file.getOriginalFilename()));
        String filename = UUID.randomUUID() + "_" + originalFilename;

        URL presignedUrl = urlService.presignedUploadUrl(filename, mimeType);

        return ResponseEntity.ok(new UrlResponse(presignedUrl.toString(), filename));
    }

    @GetMapping("/api/file/download/{filename}")
    public ResponseEntity<UrlResponse> downloadFile(@PathVariable("filename") String filename) throws IOException {

        URL presignedUrl = urlService.presignedDownloadUrl(filename);

        return ResponseEntity.ok(new UrlResponse(presignedUrl.toString(), filename));
    }

    @GetMapping("/api/proxy/image")
    public ResponseEntity<byte[]> getProxyImage(@RequestParam("filename") String filename) throws IOException {

        URL presignedUrl = urlService.presignedDownloadUrl(filename);

        InputStream inputStream = presignedUrl.openStream();
        byte[] imageBytes = inputStream.readAllBytes();
        String mimeType = URLConnection.guessContentTypeFromName(filename);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(mimeType));

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }

    // 앨범 생성
    @PostMapping("/api/album-create")
    public ResponseEntity<ApiResponse> createAlbum(@RequestParam("id") Long id,
                                                   @RequestParam("file") MultipartFile file,
                                                   @RequestParam("title") String title) throws IOException {

        String filename = uploadImage(file);

        // 멤버 조회
        Member member = memberService.get(id);
        // 앨범 생성
        Album album = new Album();
        album.setMember(member);
        album.setTitle(title);
        album.setThumbnail(filename);

        // 회원 테이블에 앨범 저장
        member.getAlbums().add(album);
        // 회원 테이블 업데이트 -> 앨범도 같이 저장
        memberService.update(member);

        return ResponseEntity.ok(new ApiResponse(GlobalStatus.OK));
    }

    // 앨범 내 사진 리스트 조회
    @GetMapping("/api/album/{id}")
    public ResponseEntity<List<UrlResponse>> getAlbumDetail(@PathVariable("id") Long id) {
        Album album = albumService.get(id);
        List<Photo> photoList = album.getPhotos();
        List<UrlResponse> urlList = new ArrayList<>();

        for(Photo photo : photoList) {
            String filename = photo.getFileName();
            String proxyUrl = "/api/proxy/image?filename=" + URLEncoder.encode("thumbnail/"+filename, StandardCharsets.UTF_8);
            urlList.add(new UrlResponse(proxyUrl, filename));
        }

        if(urlList.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }else {
            return ResponseEntity.ok(urlList);
        }
    }

    @PostMapping("/api/album/photo/upload")
    public ResponseEntity<ApiResponse> uploadPhoto(@RequestParam("id") Long id,
                                                   @RequestParam("file") MultipartFile file) throws IOException {

        String filename = uploadImage(file);

        Photo photo = new Photo();
        photo.setFileName(filename);
        photo.setAlbum(albumService.get(id));
        photoService.create(photo);

        return ResponseEntity.ok(new ApiResponse(GlobalStatus.OK));
    }

    public String uploadImage(MultipartFile file) throws IOException {

        String originalFilename = file.getOriginalFilename();
        String mimeType = file.getContentType();
        String filename = UUID.randomUUID() + "_" + originalFilename;

        byte[] bytes = file.getBytes();

        InputStream originalInputStream = new ByteArrayInputStream(bytes);
//        urlService.imageUpload("original/"+filename, originalInputStream, mimeType);

        ByteArrayOutputStream thumbnailOutputStream = new ByteArrayOutputStream();
        Thumbnails.of(new ByteArrayInputStream(bytes))
                .width(400)
                .outputFormat("jpeg")
                .outputQuality(0.5)
                .toOutputStream(thumbnailOutputStream);

        InputStream thumbnailInputStream = new ByteArrayInputStream(thumbnailOutputStream.toByteArray());

//        urlService.imageUpload("thumbnail/"+filename, thumbnailInputStream, "image/jpeg");

        return filename;
    }
}
