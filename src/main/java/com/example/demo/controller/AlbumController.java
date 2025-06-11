package com.example.demo.controller;

import com.example.demo.entity.Album;
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

        Member member = memberService.get(6L);

        List<Album> albumList = member.getAlbums();
        List<String> urlList = new ArrayList<>();

        for(Album album : albumList) {
            String filename = album.getThumbnail();
            String presignedUrl = urlService.getPresignedUrl(filename, Duration.ofMinutes(15)).toString();
            urlList.add(presignedUrl);
        }

        model.addAttribute("albumList", albumList);
        model.addAttribute("urlList", urlList);

        return "albums";
    }

}
