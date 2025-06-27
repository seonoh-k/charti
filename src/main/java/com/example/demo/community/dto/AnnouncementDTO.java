package com.example.demo.community.dto;

import com.example.demo.community.entity.CommunityBoard;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class AnnouncementDTO {
    private Long id;
    private String title;
    private String content;
    private String createdAt;

    public AnnouncementDTO(CommunityBoard communityBoard) {
        this.id = communityBoard.getCommunityId();
        this.title = communityBoard.getTitle();
        this.content = communityBoard.getContent();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        this.createdAt = communityBoard.getCreatedAt().format(formatter);
    }
}
