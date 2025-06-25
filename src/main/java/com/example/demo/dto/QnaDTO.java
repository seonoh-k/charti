package com.example.demo.dto;

import com.example.demo.entity.Qna;
import com.example.demo.enums.QnaCategory;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class QnaDTO {
    private final Long id;
    private final String name;
    private final String nickname;
    private final QnaCategory category;
    private final String title;
    private final String content;
    @JsonProperty("isPublic")
    private boolean isPublic;
    @JsonProperty("isAnswered")
    private boolean isAnswered;
    private final String createdAt;

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
    }
}
