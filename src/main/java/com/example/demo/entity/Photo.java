package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Photo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "photo_id")
    private Long id; // 사진 아이디

    private String fileName; // 사진 파일 이름

    @Column(columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean isPublic; // 공개 여부

    @ManyToOne // 앨범 테이블과 N:1 관계 설정
    @JoinColumn(name = "album_id")
    private Album album;
}
