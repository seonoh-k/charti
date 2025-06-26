package com.example.demo.dto;

import com.example.demo.entity.Qna;
import com.example.demo.enums.QnaCategory;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class QnaDTO {
    private Long id;
    private String name;
    private String nickname;
    private QnaCategory category;
    private String title;
    private String content;
    @JsonProperty("isPublic")
    private boolean isPublic;
    @JsonProperty("isAnswered")
    private boolean isAnswered;
    private String createdAt;
    @JsonProperty("deleted")
    private boolean deleted;

    public QnaDTO(Qna qna) {
        this.id = qna.getId();
        this.name = qna.getUsers().getName();
        this.nickname = qna.getUsers().getNickname();
        this.category = qna.getCategory();
        this.title = qna.getTitle();
        this.content = qna.getContent();
        this.isPublic = qna.isPublic();
        this.isAnswered = qna.isAnswered();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        this.createdAt = qna.getCreatedAt().format(formatter);
        this.deleted = qna.isDeleted();
    }

    public void entityToDTO(Qna qna) {
        this.id = qna.getId();
        this.name = qna.getUsers().getName();
        this.nickname = qna.getUsers().getNickname();
        this.category = qna.getCategory();
        this.title = qna.getTitle();
        this.content = qna.getContent();
        this.isPublic = qna.isPublic();
        this.isAnswered = qna.isAnswered();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        this.createdAt = qna.getCreatedAt().format(formatter);
        this.deleted = qna.isDeleted();
    }
}
