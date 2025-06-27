package com.example.demo.community.entity;

import com.example.demo.entity.BaseEntity;
import com.example.demo.enums.AgeGroup;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "community_board")
public class CommunityBoard extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long communityId;

    @Column(nullable = false)
    private Long usersId;

    @Column(length = 50, nullable = false)
    private String category;             // 부모 카테고리 (ex. "parentingInformation")

    @Column(length = 50, nullable = false)
    private String category2;            // 하위 카테고리

    @Column(name = "age_group", nullable = true)
    private AgeGroup ageGroup;             // 연령대

    @Column(length = 255, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 1)
    private String status = "Y";

    @Column(nullable = false)
    private Integer views = 0;
}

