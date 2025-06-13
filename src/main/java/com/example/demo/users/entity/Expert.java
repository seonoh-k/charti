package com.example.demo.users.entity;

import com.example.demo.entity.Address;
import com.example.demo.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expert extends BaseEntity{

    @Id
    @Column(name = "users_id")
    private Long id;

    @OneToOne()
    @MapsId()
    @JoinColumn(name = "users_id")
    private Users users;
    // 전공
    private String major;
    // 자격증파일
    private String license;
    // 승인여부
    private Boolean isApproved;

    @OneToOne()
    private Address address;





}
