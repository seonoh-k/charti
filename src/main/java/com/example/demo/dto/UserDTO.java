package com.example.demo.dto;

import com.example.demo.users.entity.Member;
import com.example.demo.users.entity.Role;
import com.example.demo.users.entity.Users;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserDTO {

    private Long id;
    private String uuid;
    private String name;
    private String username;
    private String nickname;
    private String password;
    private String role;
    private String provider;
    private String providerId;
    private String smsIdToken; // sms 인증
    private LocalDateTime createdAt;
    private String phoneNumber;

    private AddressDTO address;

        /**
     * [✅ toEntity 메서드 추가 이유]
     * - AuthService에서 getLoginUser()를 통해 로그인한 사용자 정보를 UserDTO로 가져오는데,
     *   실제 도메인 로직(예: 문진 제출, 자녀 등록 등)에서는 Users 엔티티 객체가 필요함.
     * - 따라서 DTO를 다시 엔티티로 변환해주는 toEntity() 메서드를 명시적으로 정의함.
     *
     * [사용 예]
     * - authService.getLoginUser().toEntity() 를 통해 현재 로그인 사용자를 Users 객체로 변환
     */
    public Users toEntity() {
        return Users.builder()
                .id(this.id)
                .uuid(this.uuid)
                .name(this.name)
                .nickname(this.nickname)
                .username(this.username)
                .password(this.password)
                .role(Role.valueOf(this.role))
                .provider(this.provider)
                .providerId(this.providerId)
                .phoneNumber(this.phoneNumber)
                .build();
    }

    public UserDTO(Users user) {
        this.id = user.getId();
        this.nickname = user.getNickname();
    }
}
