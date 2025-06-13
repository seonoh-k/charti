package com.example.demo.users.entity;

import com.example.demo.entity.Address;
import com.example.demo.entity.BaseEntity;
import com.example.demo.entity.Group;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Manager extends BaseEntity {

    @Id
    @Column(name = "users_id")
    private Long id;

    @OneToOne()
    @MapsId
    @JoinColumn(name = "users_id")
    private Users users;

    @OneToOne
    @JoinColumn(name = "group_id")
    private Group group;

    private Boolean isApproved;

    private String organization; // 유치원, 어린이집 등



}
