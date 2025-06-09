package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "child")
@Getter
@Setter
public class Child {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "child_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    @JsonBackReference
    private Member parent;  // 부모 회원 참조

    @Column(nullable = false)
    private String name;

    private String nickname;
    private LocalDateTime birthday;
    private String weight;
    private String height;
    private String gender;
    private Integer birthOrder;
    private Boolean riskGroup;
}
