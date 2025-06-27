package com.example.demo.community.entity;

import com.example.demo.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "comments")
public class Comment extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long communityId;

    @Column(nullable = false)
    private Long usersId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
}
