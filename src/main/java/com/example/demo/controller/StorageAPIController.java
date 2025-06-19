package com.example.demo.controller;

import com.example.demo.dto.PhotoDTO;
import com.example.demo.dto.UrlResponse;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.entity.Album;
import com.example.demo.entity.Photo;
import com.example.demo.service.AlbumService;
import com.example.demo.service.PhotoService;
import com.example.demo.service.PresignedUrlService;
import com.example.demo.users.entity.Member;
import com.example.demo.users.service.AuthService;
import com.example.demo.users.service.MemberService;
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
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StorageAPIController {

    private final AuthService authService;
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

    @GetMapping("/api/file/download")
    public ResponseEntity<UrlResponse> downloadFile(@RequestParam("filename") String filename) throws IOException {

        URL presignedUrl = urlService.presignedDownloadUrl(filename);

        return ResponseEntity.ok(new UrlResponse(presignedUrl.toString(), filename));
    }

    @GetMapping("/api/proxy/image")
    public ResponseEntity<byte[]> getProxyImage(@RequestParam("filename") String filename) throws IOException {

        URL presignedUrl = urlService.getPresignedUrl(filename, Duration.ofMinutes(30));

        InputStream inputStream = presignedUrl.openStream();
        byte[] imageBytes = inputStream.readAllBytes();
        String mimeType = URLConnection.guessContentTypeFromName(filename);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(mimeType));

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }

    // 앨범 생성
    @PostMapping("/api/album-create")
    public ResponseEntity<ApiResponse> createAlbum(@RequestParam("file") MultipartFile file,
                                                   @RequestParam("title") String title) throws IOException {

        String filename = uploadImage(file);

        UserDTO userDTO = authService.getLoginUser();
        // 멤버 조회
        Member member = memberService.get(userDTO.getId());
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
    public ResponseEntity<List<PhotoDTO>> getAlbumDetail(@PathVariable("id") Long id) {
        UserDTO userDTO = authService.getLoginUser();
        Album album = albumService.get(id);
        List<PhotoDTO> photoList = new ArrayList<>();


        for(Photo photo : album.getPhotos()) {
            if(album.getMember().getId().equals(userDTO.getId()) || photo.getIsPublic()){
                photoList.add(new PhotoDTO(photo));
            }
        }

        return ResponseEntity.ok(photoList);
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
    // 전문가 회원 자격증파일 업로드
    @PostMapping("/api/expert/join/license")
    public ResponseEntity<ApiResponse> uploadLicense(@RequestParam("file") MultipartFile file) throws IOException {
        // 1. 파일 이름과 MIME 타입 가져오기
        String originalFilename = file.getOriginalFilename();
        String mimeType = file.getContentType();
        List<String> allowedMimeTypes = List.of(
                "application/pdf",
                "image/jpeg",
                "image/png"
        );
        if (!allowedMimeTypes.contains(mimeType)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(GlobalStatus. TYPE_MISMATCH, "지원되지 않는 파일 형식입니다."));
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();

        if (!List.of("pdf", "jpg", "jpeg", "png").contains(extension)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(GlobalStatus.TYPE_MISMATCH, "허용되지 않는 확장자입니다."));
        }
        // 2. UUID_파일명 생성
        String filename = UUID.randomUUID() + "_" + originalFilename;
        // 3. byte[] → InputStream 변환
        byte[] bytes = file.getBytes();
        InputStream licenseInputStream = new ByteArrayInputStream(bytes);
        // 4. Cloudflare R2 업로드
        urlService.imageUpload("license/" + filename, licenseInputStream, mimeType);
        // 5. 결과 반환 (파일명만 클라이언트로 보냄)
        return ResponseEntity.ok(new ApiResponse(GlobalStatus.OK, filename));
    }




    public String uploadImage(MultipartFile file) throws IOException {

        String originalFilename = file.getOriginalFilename();
        String mimeType = file.getContentType();
        String filename = UUID.randomUUID() + "_" + originalFilename;

        byte[] bytes = file.getBytes();

        InputStream originalInputStream = new ByteArrayInputStream(bytes);
        urlService.imageUpload("original/"+filename, originalInputStream, mimeType);

        ByteArrayOutputStream thumbnailOutputStream = new ByteArrayOutputStream();
        Thumbnails.of(new ByteArrayInputStream(bytes))
                .width(400)
                .outputFormat("jpeg")
                .outputQuality(0.5)
                .toOutputStream(thumbnailOutputStream);

        InputStream thumbnailInputStream = new ByteArrayInputStream(thumbnailOutputStream.toByteArray());

        urlService.imageUpload("thumbnail/"+filename, thumbnailInputStream, "image/jpeg");

        return filename;
    }

    @PostMapping("/api/album/visibility")
    public ResponseEntity<ApiResponse> updateAlbumVisibility(@RequestParam("id") Long id,
                                                             @RequestParam("isPublic") boolean isPublic) {
        Album album = albumService.get(id);
        album.setIsPublic(isPublic);
        albumService.update(album);

        return ResponseEntity.ok(new ApiResponse(GlobalStatus.OK));
    }

    @PostMapping("/api/photo/visibility")
    public ResponseEntity<ApiResponse> updatePhotoVisibility(@RequestParam("id") Long id,
                                                             @RequestParam("isPublic") boolean isPublic) {
        Photo photo = photoService.get(id);
        photo.setIsPublic(isPublic);
        photoService.update(photo);

        return ResponseEntity.ok(new ApiResponse(GlobalStatus.OK));
    }

    @GetMapping("/api/album/delete")
    public ResponseEntity<ApiResponse> deleteAlbum(@RequestParam("id") Long id) {
        Album album = albumService.get(id);
        List<Photo> photos = album.getPhotos();

        for(Photo photo : photos) {
            urlService.deleteFile("original/" + photo.getFileName());
            urlService.deleteFile("thumbnail/" + photo.getFileName());
        }

        urlService.deleteFile("original/" + album.getThumbnail());
        urlService.deleteFile("thumbnail/" + album.getThumbnail());

        albumService.delete(id);

        return ResponseEntity.ok(new ApiResponse(GlobalStatus.OK));
    }

    @GetMapping("/api/photo/delete")
    public ResponseEntity<ApiResponse> deletePhoto(@RequestParam("id") Long id) {
        Photo photo = photoService.get(id);

        urlService.deleteFile("original/" + photo.getFileName());
        urlService.deleteFile("thumbnail/" + photo.getFileName());

        photoService.delete(id);

        return ResponseEntity.ok(new ApiResponse(GlobalStatus.OK));
    }
}
