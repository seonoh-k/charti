package com.example.demo.dto;


import com.example.demo.users.entity.Expert;
import com.example.demo.users.entity.Users;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpertDTO {
    // users_id (Manager 엔티티에서 참조)
    private Long id;
    private String name;
    private String nickname;
    private String username; // 이메일
    private String phoneNumber;
    private String license;
    private String major;
    private String career;
    private LocalDateTime createdAt;
    private Boolean isApproved;
    private Boolean deleted;
    // 엔티티 → DTO 변환 메서드
    public static ExpertDTO fromEntity(Expert expert) {
        // 추가로 필요한 데이터 작성
        return ExpertDTO.builder()
                .id(expert.getUsers().getId())                 // FK가 Users면, .getId()로 접근
                .name(expert.getUsers().getName())
                .nickname(expert.getUsers().getNickname())
                .username(expert.getUsers().getUsername())    // username == email
                .phoneNumber(expert.getUsers().getPhoneNumber())
                .license(expert.getLicense())
                .major(expert.getMajor())
                .career(expert.getCareer())
                .isApproved(expert.getIsApproved())
                .createdAt(expert.getUsers().getCreatedAt())
                .build();
    }

    public static ExpertDTO fromEntity(Users user) {
        // 추가로 필요한 데이터 작성
        return ExpertDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .nickname(user.getNickname())
                .username(user.getUsername())    // username == email
                .phoneNumber(user.getPhoneNumber())
                .license(user.getExpert().getLicense())
                .major(user.getExpert().getMajor())
                .career(user.getExpert().getCareer())
                .isApproved(user.getExpert().getIsApproved())
                .createdAt(user.getCreatedAt())
                .build();
    }

}

