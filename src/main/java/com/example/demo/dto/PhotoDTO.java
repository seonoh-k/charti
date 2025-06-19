package com.example.demo.dto;

import com.example.demo.entity.Photo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class PhotoDTO {
    private Long id;
    private String filename;
    @JsonProperty("isPublic")
    private boolean isPublic;
    private String createdAt;

    public PhotoDTO(Photo photo) {
        this.id = photo.getId();
        this.filename = photo.getFileName();
        this.isPublic = photo.getIsPublic();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        this.createdAt = photo.getCreatedAt().format(formatter);
    }
}
