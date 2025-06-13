package com.example.demo.entity;

import com.example.demo.users.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id; // 주소 아이디

    @Column(length = 7)
    private String zipNum; // 우편 번호

    @Column(length = 30)
    private String sido; // 시/도

    @Column(length = 30)
    private String gugun; // 구/군

    @Column(length = 100)
    private String dong; // 동

    @Column(length = 30)
    private String bunji; // 번지

    @OneToOne(mappedBy = "address")
    private Member member;
    @OneToOne(mappedBy = "address")
    private Group group;
}
