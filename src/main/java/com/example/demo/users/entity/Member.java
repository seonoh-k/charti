package com.example.demo.users.entity;

import com.example.demo.entity.Address;
import com.example.demo.entity.Album;
import com.example.demo.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member")
@Getter @Setter
public class Member extends BaseEntity {

    @Id
    @Column(name = "users_id")
    private Long id;

    @OneToOne()
    @MapsId()
    @JoinColumn(name = "users_id")
    private Users users;

    @OneToOne
    @JoinColumn(name = "address_id")
    private Address address;

    private Integer totalPoint;

    // 자녀랑 양방향관계 / 부모 아이디 탈퇴시 자녀 2명이상 등록되어있을경우 전부 삭제
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Child> children = new ArrayList<>();

    // 앨범 테이블과 1:N 관계 설정.
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Album> albums = new ArrayList<>();
}