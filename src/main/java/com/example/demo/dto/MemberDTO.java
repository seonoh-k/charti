package com.example.demo.dto;

import com.example.demo.users.entity.Member;
import com.example.demo.users.entity.Users;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MemberDTO {
    // users_id
    private Long id;
    private String name;
    private String nickname;
    private String email;
    private String phoneNumber; // 유저의 전화번호
    private String provider;
    private Integer totalPoint;
    private LocalDateTime createdAt;

    // 엔티티 → DTO 변환 메서드

    public static MemberDTO fromEntity(Member member) {
        // 추가로 필요한 데이터 작성
        return MemberDTO.builder()
                .id(member.getUsers().getId())
                .name(member.getUsers().getName())
                .nickname(member.getNickname())
                .email(member.getUsers().getUsername())    // username == email
                .phoneNumber(member.getUsers().getPhoneNumber())
                .provider(member.getUsers().getProvider())
                .totalPoint(member.getTotalPoint())
                .createdAt(member.getUsers().getCreatedAt())
                .build();
    }
    public static MemberDTO fromEntity(Users user) {
        // 추가로 필요한 데이터 작성
        return MemberDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .nickname(user.getNickname())
                .email(user.getUsername())    // username == email
                .phoneNumber(user.getPhoneNumber())
                .provider(user.getProvider())
                .totalPoint(user.getMember().getTotalPoint())
                .createdAt(user.getCreatedAt())
                .build();
    }



}