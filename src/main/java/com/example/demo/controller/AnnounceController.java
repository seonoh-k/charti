package com.example.demo.controller;

import com.example.demo.community.dto.AnnouncementDTO;
import com.example.demo.community.entity.CommunityBoard;
import com.example.demo.community.service.CommunityBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AnnounceController {

    private final CommunityBoardService cbService;

    @GetMapping("/announcement")
    public String announcement(@RequestParam(value = "page", defaultValue = "1", required = false) int page,
                               Model model) {
        Page<CommunityBoard> pagedList = cbService.getPagedList(page - 1);

        List<AnnouncementDTO> boardList = new ArrayList<>();

        for(CommunityBoard board : pagedList){
            boardList.add(new AnnouncementDTO(board));
        }

        model.addAttribute("boardList", boardList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pagedList.getTotalPages());

        return "announceList";
    }

    @GetMapping("/announcement/{id}")
    public String getAnnouncement(@PathVariable Long id, Model model) {
        CommunityBoard cb = cbService.findById(id);
        AnnouncementDTO dto = new AnnouncementDTO(cb);
        model.addAttribute("anc", dto);
        return "announcement";
    }

    @GetMapping("/admin/announcement")
    public String adminAnnouncement(@RequestParam(value = "page", defaultValue = "1", required = false) int page,
                               Model model) {
        Page<CommunityBoard> pagedList = cbService.getPagedList(page - 1);

        List<AnnouncementDTO> boardList = new ArrayList<>();

        for(CommunityBoard board : pagedList){
            boardList.add(new AnnouncementDTO(board));
        }

        model.addAttribute("boardList", boardList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pagedList.getTotalPages());

        return "admin/admin/announceList";
    }

    @GetMapping("/admin/announcement/{id}")
    public String getAdminAnnouncement(@PathVariable Long id, Model model) {
        CommunityBoard cb = cbService.findById(id);
        AnnouncementDTO dto = new AnnouncementDTO(cb);
        model.addAttribute("anc", dto);
        return "admin/admin/announcement";
    }

}
