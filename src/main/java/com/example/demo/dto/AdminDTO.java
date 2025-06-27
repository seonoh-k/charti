package com.example.demo.dto;

import com.example.demo.users.entity.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDTO {

    private Long id;
    private String uuid;
    private String name;
    private String position;
    private String username;

    private String password;
    private String phoneNumber;

    private String role;

    public AdminDTO(Long id, String name, String position, String username, String password, String phoneNumber,Role role) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.role = role.name();
    }


    public AdminDTO(String name, String position, String username, String password, String phoneNumber) {
        this.name = name;
        this.position = position;
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
    }
    public AdminDTO(Long id,String uuid, String name, Role role){
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.role = role.name();
    }


}
