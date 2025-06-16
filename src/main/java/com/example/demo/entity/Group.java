package com.example.demo.entity;

import com.example.demo.users.entity.Manager;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "group_info")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group extends BaseEntity {
    // 기본키
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long id;

    // 참조키
    @OneToOne()
    @JoinColumn(name = "address_id")
    private Address address;

    // 그룹 이름
    private String name;
    // 그룹 이메일(담당자 이메일은 따로 이미 받음)
    private String email;
    // 그룹 전화번호
    private String phoneNumber;

    @OneToOne(mappedBy = "group")
    private Manager manager;

}

