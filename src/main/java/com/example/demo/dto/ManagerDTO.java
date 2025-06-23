package com.example.demo.dto;

import com.example.demo.users.entity.Manager;
import com.example.demo.users.entity.Users;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerDTO {
    // users_id (Manager 엔티티에서 참조)
    private Long id;
    private String name;
    private String username; // 이메일
    private String nickname;
    private String phoneNumber; // 담당자 전화번호
    private Long groupId; // 그룹 아이디
    private String groupName; // 그룹 이름
    private String groupEmail; // 그룹 이메일
    private Boolean isApproved;
    private Boolean deleted;
    private LocalDateTime createdAt;
    private List<ChildDTO> children;
    // 엔티티 → DTO 변환 메서드
    public static ManagerDTO fromEntity(Manager manager) {

        // 추가로 필요한 데이터 작성
        if(manager.getGroup() != null){
            return ManagerDTO.builder()
                    .id(manager.getUsers().getId())                 // FK가 Users면, .getId()로 접근
                    // 담당자 이름
                    .name(manager.getUsers().getName())
                    .nickname(manager.getUsers().getNickname())
                    .username(manager.getUsers().getUsername())    // username == email
                    .phoneNumber(manager.getUsers().getPhoneNumber())
                    .groupName(manager.getGroup().getGroupName())
                    .groupEmail(manager.getGroup().getGroupEmail())
                    .groupId(manager.getGroup().getId())
                    .isApproved(manager.getIsApproved())
                    .createdAt(manager.getUsers().getCreatedAt())
                    .build();
        } else {
            return ManagerDTO.builder()
                    .id(manager.getUsers().getId())                 // FK가 Users면, .getId()로 접근
                    .name(manager.getUsers().getName())
                    .nickname(manager.getUsers().getNickname())
                    .phoneNumber(manager.getUsers().getPhoneNumber())
                    .username(manager.getUsers().getUsername())
                    .groupId(manager.getGroup().getId())
                    .groupName(manager.getGroup().getGroupName())
                    .groupEmail(manager.getGroup().getGroupEmail())
                    .isApproved(manager.getIsApproved())
                    .createdAt(manager.getUsers().getCreatedAt())
                    .build();
        }

    }

    public static ManagerDTO fromEntity(Users user) {
        // 추가로 필요한 데이터 작성
        return ManagerDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .nickname(user.getNickname())
                .username(user.getUsername())    // username == email
                .phoneNumber(user.getPhoneNumber())
                .groupId(user.getManager().getGroup().getId())
                .groupName(user.getManager().getGroup().getGroupName())
                .groupEmail(user.getManager().getGroup().getGroupEmail())
                .isApproved(user.getManager().getIsApproved())
                .createdAt(user.getCreatedAt())
                .build();
    }
    public ManagerDTO(Long id, String name, String username, String nickname,
                      String phoneNumber, Long groupId, String groupName, String groupEmail,
                      Boolean isApproved, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.nickname = nickname;
        this.groupId = groupId;
        this.phoneNumber = phoneNumber;
        this.groupName = groupName;
        this.groupEmail = groupEmail;
        this.isApproved = isApproved;
        this.createdAt = createdAt;
    }
    public ManagerDTO(Long id, String name, String username, String nickname,
                      String phoneNumber, Long groupId, String groupName, String groupEmail,
                      Boolean isApproved, LocalDateTime createdAt, Boolean deleted) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.groupId = groupId;
        this.groupName = groupName;
        this.groupEmail = groupEmail;
        this.isApproved = isApproved;
        this.createdAt = createdAt;
        this.deleted = deleted;
    }


}
