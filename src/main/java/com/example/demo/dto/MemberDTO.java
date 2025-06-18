package com.example.demo.dto;

import com.example.demo.users.entity.Member;
import com.example.demo.users.entity.Users;
import lombok.*;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {
    // users_id
    private Long id;
    private String name;
    private String email;
    private String phoneNumber; // 유저의 전화번호
    private Integer totalPoint;

    // 엔티티 → DTO 변환 메서드

    public static MemberDTO fromEntity(Member member) {
        // 추가로 필요한 데이터 작성
        return MemberDTO.builder()
                .id(member.getUsers().getId())
                .name(member.getUsers().getName())
                .email(member.getUsers().getUsername())    // username == email
                .phoneNumber(member.getUsers().getPhoneNumber())
                .totalPoint(member.getTotalPoint())
                .build();
    }
    public static MemberDTO fromEntity(Users user) {
        // 추가로 필요한 데이터 작성
        return MemberDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getUsername())    // username == email
                .phoneNumber(user.getPhoneNumber())
                .totalPoint(user.getMember().getTotalPoint())
                .build();
    }



}