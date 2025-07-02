package com.example.demo.users.entity;


import com.example.demo.entity.BaseEntity;
import com.example.demo.entity.Qna;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users extends BaseEntity{

    @Id
    @Column(name = "users_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uuid;

    private String username;

    private String nickname;

    private String name;

    private String password;

    private String phoneNumber;

    private String profileImage;

    private String provider;

    private String providerId;

    @Enumerated(value = EnumType.STRING)
    private Role role;

    // 외래키를 소유하지 않는 측에서 작성
    @OneToOne(mappedBy = "users")
    private Member member;

    @OneToOne(mappedBy = "users")
    private Manager manager;

    @OneToOne(mappedBy = "users")
    private Expert expert;

    // QNA 테이블과 1:N 관계 설정
    @OneToMany(mappedBy = "users", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Qna> qnas = new ArrayList<>();
}
