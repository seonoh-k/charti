package com.example.demo.entity;

import com.example.demo.users.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter @Setter
public class Album extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "album_id")
    private Long id; // 앨범 아이디

    @ManyToOne
    @JoinColumn(name = "users_id")
    private Member member; // 일반 회원 참조

    @Column(length = 50)
    private String title; // 앨범 제목

    private String thumbnail; // 썸네일 파일 이름

    @Column(columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean isPublic; // 공개 여부

    // 사진 테이블과 1:N 관계 설정
    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Photo> photos; // 앨범에 포함되는 사진 리스트
}
