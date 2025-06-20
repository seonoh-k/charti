package com.example.demo.fcm.dto;

import com.example.demo.enums.FcmCategory;
import com.example.demo.fcm.entity.Notice;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class NoticeDto {
    private Long id;
    private String title;
    private String body;
    private FcmCategory category;
    private LocalDateTime sentAt;
    private String url;

    public static NoticeDto from(Notice n) {
        NoticeDto dto = new NoticeDto();
        dto.setId(n.getId());
        dto.setTitle(n.getTitle());
        dto.setBody(n.getBody());
        dto.setCategory(n.getCategory());
        dto.setSentAt(n.getSentAt());
        dto.setUrl(n.getUrl());
        return dto;
    }
}
