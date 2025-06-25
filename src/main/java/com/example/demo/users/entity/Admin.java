package com.example.demo.users.entity;

import com.example.demo.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long id;
    private String name;
    private String username;
    private String password;
    private String phoneNumber;

    @Enumerated(value = EnumType.STRING)
    private Role role;

}
