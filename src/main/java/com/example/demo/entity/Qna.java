package com.example.demo.entity;

import com.example.demo.enums.QnaCategory;
import com.example.demo.users.entity.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Qna extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qna_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "users_id")
    private Users users;

    @Enumerated(EnumType.STRING)
    private QnaCategory category;

    private String title;
    private String content;

    // 공개 여부 - 디폴트 true : 공개
    private boolean isPublic = true;

    // 답변 여부 - 디폴트 false : 답변 대기
    private boolean isAnswered = false;

    @OneToMany(mappedBy = "qna", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QnaAnswer> answers = new ArrayList<>();
}
