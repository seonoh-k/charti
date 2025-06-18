package com.example.demo.dto;

import com.example.demo.users.entity.Manager;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerDTO {
    // users_id (Manager 엔티티에서 참조)
    private Long id;
    private String name;
    private String email;
    private String phoneNumber; // 담당자 전화번호
    private String groupName; // 그룹 이름
    private Long groupId; // 그룹 아이디
    private String groupEmail; // 그룹 이메일
    private Boolean isApproved;

    // 엔티티 → DTO 변환 메서드
    public static ManagerDTO fromEntity(Manager manager) {

        // 추가로 필요한 데이터 작성
        if(manager.getGroup() != null){
            return ManagerDTO.builder()
                    .id(manager.getUsers().getId())                 // FK가 Users면, .getId()로 접근
                    // 담당자 이름
                    .name(manager.getUsers().getName())
                    .email(manager.getUsers().getUsername())    // username == email
                    .phoneNumber(manager.getUsers().getPhoneNumber())
                    .groupName(manager.getGroup().getGroupName())
                    .isApproved(manager.getIsApproved())
                    .build();
        } else {
            return ManagerDTO.builder()
                    .id(manager.getUsers().getId())                 // FK가 Users면, .getId()로 접근
                    .name(manager.getUsers().getName())
                    .phoneNumber(manager.getUsers().getPhoneNumber())
                    .email(manager.getUsers().getUsername())
                    .groupName(manager.getGroup().getGroupName())
                    .isApproved(manager.getIsApproved())
                    .build();
        }

    }

}
