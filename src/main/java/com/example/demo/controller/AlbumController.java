package com.example.demo.controller;

import com.example.demo.dto.AlbumDTO;
import com.example.demo.dto.PhotoDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.Album;
import com.example.demo.entity.Photo;
import com.example.demo.service.AlbumService;
import com.example.demo.service.PhotoService;
import com.example.demo.users.entity.Member;
import com.example.demo.users.service.AuthService;
import com.example.demo.users.service.ManagerService;
import com.example.demo.users.service.MemberService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AlbumController {

    private final AuthService authService;
    private final AlbumService albumService;
    private final PhotoService photoService;
    private final MemberService memberService;
    private final ManagerService managerService;

    @GetMapping({"/albumList","/albumList/{id}"})
    public String getAlbumList(@PathVariable(value = "id", required = false) Long id, Model model) {

        UserDTO userDTO = authService.getLoginUser();
        Long targetId = id == null ? userDTO.getId() : id;

        try {
            Member member = memberService.get(targetId);
            UserDTO owner = new UserDTO(member.getUsers());

            model.addAttribute("owner", owner);
        }catch (NoSuchElementException | EntityNotFoundException e) {
            return "redirect:/";
        }

        Page<Album> albums = albumService.getPagedList(targetId, 0);
        List<AlbumDTO> albumList = new ArrayList<>();
        List<String> urlList = new ArrayList<>();

        boolean isOwner = userDTO.getId().equals(targetId);

        for(Album album : albums) {
            if(isOwner || album.getIsPublic()){
                albumList.add(new AlbumDTO(album));
                String filename = album.getThumbnail();
                String presignedUrl = "/api/proxy/image?filename=thumbnail/"+filename;
                urlList.add(presignedUrl);
            }
        }
       log.info(albums.toString());
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("loginUser", userDTO);
        model.addAttribute("albumList", albumList);
        model.addAttribute("urlList", urlList);
        model.addAttribute("isLastPage", albums.isLast());

        return "albumList";
    }

    @GetMapping("/album/{id}")
    public String getAlbum(@PathVariable("id") Long id, Model model) {

        Album album = albumService.get(id);
        UserDTO userDTO = authService.getLoginUser();

        if(!album.getIsPublic() && !userDTO.getId().equals(album.getMember().getId())) {
            return "redirect:/";
        }

        boolean isOwner = userDTO.getId().equals(album.getMember().getId());
        Page<Photo> photos = photoService.getPagedList(id, 0);
        List<PhotoDTO> photoList = new ArrayList<>();

        for(Photo photo : photos.getContent()) {
            if(isOwner || photo.getIsPublic()){
                photoList.add(new PhotoDTO(photo));
            }
        }

        model.addAttribute("isOwner", isOwner);
        model.addAttribute("ownerId", album.getMember().getId());
        model.addAttribute("album", album);
        model.addAttribute("url", "/api/proxy/image?filename=thumbnail/"+album.getThumbnail());
        model.addAttribute("photoList", photoList);
        model.addAttribute("isLastPage", photos.isLast());

        return "album";
    }

}
