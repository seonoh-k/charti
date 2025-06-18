package com.example.demo.dto;


import com.example.demo.users.entity.Expert;
import com.example.demo.users.entity.Manager;
import com.example.demo.users.entity.Users;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpertDTO {
    // users_id (Manager 엔티티에서 참조)
    private Long id;
    private String name;
    private String email;
    private String phoneNumber; // 담당자 전화번호
    private String license; // 그룹 이름
    private String major; // 그룹 아이디
    private String career; // 그룹 이메일
    private Boolean isApproved;

    // 엔티티 → DTO 변환 메서드
    public static ExpertDTO fromEntity(Expert expert) {
        // 추가로 필요한 데이터 작성
            return ExpertDTO.builder()
                    .id(expert.getUsers().getId())                 // FK가 Users면, .getId()로 접근
                    .name(expert.getUsers().getName())
                    .email(expert.getUsers().getUsername())    // username == email
                    .phoneNumber(expert.getUsers().getPhoneNumber())
                    .license(expert.getLicense())
                    .major(expert.getMajor())
                    .career(expert.getCareer())
                    .isApproved(expert.getIsApproved())
                    .build();
    }

    public static ExpertDTO fromEntity(Users user) {
        // 추가로 필요한 데이터 작성
        return ExpertDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getUsername())    // username == email
                .phoneNumber(user.getPhoneNumber())
                .license(user.getExpert().getLicense())
                .major(user.getExpert().getMajor())
                .career(user.getExpert().getCareer())
                .isApproved(user.getExpert().getIsApproved())
                .build();
    }

}

