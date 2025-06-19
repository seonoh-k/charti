package com.example.demo.controller;

import com.example.demo.dto.PhotoDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.Album;
import com.example.demo.entity.Photo;
import com.example.demo.service.AlbumService;
import com.example.demo.users.entity.Member;
import com.example.demo.users.service.AuthService;
import com.example.demo.users.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AlbumController {

    private final AuthService authService;
    private final AlbumService albumService;
    private final MemberService memberService;


    @GetMapping("/albumList")
    public String getAlbumList(@RequestParam("id") Long id, Model model) {

        UserDTO userDTO = authService.getLoginUser();
        Member member = memberService.get(id);

        List<Album> albumList = member.getAlbums();
        List<String> urlList = new ArrayList<>();

        boolean isOwner = userDTO.getId().equals(member.getId());

        for(Album album : albumList) {
            if(id.equals(userDTO.getId()) || album.getIsPublic()){
                String filename = album.getThumbnail();
                String presignedUrl = "/api/proxy/image?filename=thumbnail/"+filename;
                urlList.add(presignedUrl);
            }
        }

        model.addAttribute("isOwner", isOwner);
        model.addAttribute("albumList", albumList);
        model.addAttribute("urlList", urlList);

        return "albumList";
    }

    @GetMapping("/album/{id}")
    public String getAlbum(@PathVariable("id") Long id, Model model) {

        Album album = albumService.get(id);
        UserDTO userDTO = authService.getLoginUser();

        if(!album.getIsPublic()) {
            return "redirect:/";
        }

        boolean isOwner = userDTO.getId().equals(album.getMember().getId());
        List<PhotoDTO> photoList = new ArrayList<>();

        for(Photo photo : album.getPhotos()) {
            if(isOwner || photo.getIsPublic()){
                photoList.add(new PhotoDTO(photo));
            }
        }

        model.addAttribute("isOwner", isOwner);
        model.addAttribute("album", album);
        model.addAttribute("url", "/api/proxy/image?filename=thumbnail/"+album.getThumbnail());
        model.addAttribute("photoList", photoList);

        return "album";
    }

}
