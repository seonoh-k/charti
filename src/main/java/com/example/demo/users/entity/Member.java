package com.example.demo.users.entity;

import com.example.demo.entity.Address;
import com.example.demo.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member")
@Getter @Setter
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "users_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String nickname;

    @OneToOne
    @JoinColumn(name = "address_id")
    private Address address;

    @Column
    private String phone;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "total_point")
    private Integer totalPoint;

    // 자녀랑 양방향관계 / 부모 아이디 탈퇴시 자녀 2명이상 등록되어있을경우 전부 삭제
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Child> children = new ArrayList<>();
}
