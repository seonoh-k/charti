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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member{

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

    /**
     * [편의 메서드] 사용자 이름을 가져오기 위한 메서드
     * - Member는 Users와 1:1 매핑되어 있어 이름(name)은 Users에 존재함
     * - Thymeleaf 등 View 단에서 member.name 접근이 안 되므로,
     *   member.getUsers().getName() 대신 member.getName()으로 바로 쓸 수 있도록 제공
     * - 실제 DB 컬럼이 아님 (ERD나 테이블에는 없음)
     */
    public String getName() {
        return this.users != null ? this.users.getName() : null;
    }
    public String getEmail() {
        return this.users != null ? this.users.getUsername() : null;
    }

    public String getPhoneNumber() {
        return this.users != null ? this.users.getPhoneNumber() : null;
    }

    public String getNickname() {
        return this.users != null ? this.users.getNickname() : null;
    }
}