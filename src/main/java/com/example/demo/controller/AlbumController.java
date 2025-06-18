package com.example.demo.controller;

import com.example.demo.dto.PhotoDTO;
import com.example.demo.entity.Album;
import com.example.demo.entity.Photo;
import com.example.demo.service.AlbumService;
import com.example.demo.service.PresignedUrlService;
import com.example.demo.users.entity.Member;
import com.example.demo.users.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AlbumController {

    private final PresignedUrlService urlService;
    private final AlbumService albumService;
    private final MemberService memberService;


    @GetMapping("/albums")
    public String getAlbumList(Model model) {

        Member member = memberService.get(50L);

        List<Album> albumList = member.getAlbums();
        List<String> urlList = new ArrayList<>();

        for(Album album : albumList) {
            String filename = album.getThumbnail();
            String presignedUrl = "/api/proxy/image?filename=thumbnail/"+filename;
            urlList.add(presignedUrl);
        }

        model.addAttribute("albumList", albumList);
        model.addAttribute("urlList", urlList);

        return "albums";
    }

    @GetMapping("/album/{id}")
    public String getAlbum(@PathVariable("id") Long id, Model model) {

        Album album = albumService.get(id);
        List<PhotoDTO> photoList = new ArrayList<>();

        for(Photo photo : album.getPhotos()) {
            PhotoDTO photoDTO = new PhotoDTO(photo);
            photoList.add(photoDTO);
        }

        model.addAttribute("id", id);
        model.addAttribute("album", album);
        model.addAttribute("url", "/api/proxy/image?filename=thumbnail/"+album.getThumbnail());
        model.addAttribute("photoList", photoList);

        return "album";
    }

}
