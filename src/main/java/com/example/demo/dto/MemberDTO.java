package com.example.demo.dto;

import com.example.demo.users.entity.Member;
import com.example.demo.users.entity.Users;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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
    private String username;
    private String phoneNumber; // 유저의 전화번호
    private String provider;
    private Integer totalPoint;
    private LocalDateTime createdAt;
    private Boolean deleted;
    private List<ChildDTO> children;

    // 엔티티 → DTO 변환 메서드

    public static MemberDTO fromEntity(Member member) {
        return MemberDTO.builder()
                .id(member.getUsers().getId())
                .name(member.getUsers().getName())
                .nickname(member.getNickname())
                .username(member.getUsers().getUsername())
                .phoneNumber(member.getUsers().getPhoneNumber())
                .provider(member.getUsers().getProvider())
                .totalPoint(member.getTotalPoint())
                .createdAt(member.getUsers().getCreatedAt())
                .children(member.getChildren().stream()
                                .map(ChildDTO::fromEntity)
                                .toList())
                .build();
    }
    public static MemberDTO fromEntity(Users user) {
        // 추가로 필요한 데이터 작성
        return MemberDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .nickname(user.getNickname())
                .username(user.getUsername())    // username == email
                .phoneNumber(user.getPhoneNumber())
                .provider(user.getProvider())
                .totalPoint(user.getMember().getTotalPoint())
                .createdAt(user.getCreatedAt())
                .build();
    }
    public MemberDTO(Long id, String name, String nickname, String username,
                     String phoneNumber, String provider, Integer totalPoint,
                     LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.nickname = nickname;
        this.username = username; // email
        this.phoneNumber = phoneNumber;
        this.provider = provider;
        this.totalPoint = totalPoint;
        this.createdAt = createdAt;
    }
    public MemberDTO(Long id, String name, String nickname, String username,
                     String phoneNumber, String provider, Integer totalPoint,
                     LocalDateTime createdAt,Boolean deleted) {
        this.id = id;
        this.name = name;
        this.nickname = nickname;
        this.username = username; // email
        this.phoneNumber = phoneNumber;
        this.provider = provider;
        this.totalPoint = totalPoint;
        this.createdAt = createdAt;
        this.deleted = deleted;
    }



}