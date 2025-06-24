package com.example.demo.dto.request;

import com.example.demo.dto.AddressDTO;
import com.example.demo.dto.info.AddressInfo;
import com.example.demo.dto.info.CommonInfo;
import com.example.demo.dto.info.GroupInfo;
import com.example.demo.dto.info.ManagerInfo;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;

/**
 * Json Data Format : 변수명 일치
 * {
 *   "commonInfo": {
 *     "username": "wonjang@naver.com",
 *     "uuid": "550e8400-e29b-41d4-a716-446655440000",
 *     "password": "P@ssw0rd!",
 *     "nickname": "야쿠자",
 *     "name": "김원장",
 *     "role": "ROLE_MANAGER",
 *     "phoneNumber": "010-1111-2222",
 *     "provider": "local",
 *     "providerId": null,
 *     "profileImage": "https://example.com/images/manager01.png"
 *   },
 *   "managerInfo": {
 *     "position": "심리상담사"
 *   },
 *   "groupInfo": {
 *     "targetGroup": "심리치료",
 *     "email": "contact@mindgroup.kr",
 *     "name": "떡잎유치원",
 *     "phoneNumber": "02-1234-5678"
 *   },
 *   "addressInfo": {
 *     "zipNum": "135-919",
 *     "sido": "서울특별시",
 *     "gugun": "강남구",
 *     "dong": "논현동",
 *     "bunji": "123-45",
 *     "addressDetail": "5층 상담실"
 *   }
 * }
 */
@Data
@Builder
public class ManagerJoinRequest {

    // 회원가입에 필요한 공통 속성 모음
    @Valid  // 유효성 검사를 위한 어노테이션
    private CommonInfo commonInfo;

    // 각 사용자의 특성에 따라 필요한 정보
    private ManagerInfo managerInfo;
    private GroupInfo groupInfo;

    // 주소가 필요할 경우
    private AddressInfo addressInfo;


}
