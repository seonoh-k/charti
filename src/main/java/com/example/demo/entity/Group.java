package com.example.demo.entity;

import com.example.demo.enums.TargetGroup;
import com.example.demo.users.entity.Child;
import com.example.demo.users.entity.Manager;
import jakarta.persistence.*;
import lombok.*;

import java.lang.annotation.Target;
import java.util.List;

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
    private String groupName;
    // 그룹 대표 이메일(담당자 이메일은 따로 이미 받음)
    private String groupEmail;
    // 그룹 전화번호 회사전화기
    private String groupPhoneNumber;
    // 타겟 그룹(유치원, 어린이집, 보육원)
    @Column(name = "target_group", nullable = false)
    private TargetGroup targetGroup;

    @OneToOne(mappedBy = "group")
    private Manager manager;

    @OneToMany(mappedBy = "group")
    private List<Child> children;
}

