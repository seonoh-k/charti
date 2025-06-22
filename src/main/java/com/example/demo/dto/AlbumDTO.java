package com.example.demo.dto;

import com.example.demo.entity.Album;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class AlbumDTO {
    private Long id;
    private String title;
    private String thumbnail;
    @JsonProperty("isPublic")
    private boolean isPublic;
    private String createdAt;

    public AlbumDTO(Album album) {
        this.id = album.getId();
        this.title = album.getTitle();
        this.thumbnail = album.getThumbnail();
        this.isPublic = album.getIsPublic();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        this.createdAt = album.getCreatedAt().format(formatter);
    }
}
