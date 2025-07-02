package com.example.demo.dto.request;

import com.example.demo.dto.AddressDTO;
import com.example.demo.dto.info.AddressInfo;
import com.example.demo.dto.info.CommonInfo;
import com.example.demo.dto.info.ExpertInfo;
import jakarta.validation.Valid;
import lombok.*;

/**
 * {
 *   "commonInfo": {
 *     "username": "expert123",
 *     "password": "securePw",
 *     "name": "홍길동",
 *     "nickname": "전문가",
 *     "phoneNumber": "010-1234-5678",
 *     "provider": "local",
 *     "providerId": null
 *   },
 *   "expertInfo": {
 *     "major": "심리상담",
 *     "license": "LIC-98765"
 *   },
 *   "addressInfo": {
 *     "zipNum": "12345",
 *     "sido": "서울특별시",
 *     "gugun": "강남구",
 *     "dong": "삼성동",
 *     "bunji": "123-45",
 *     "addressDetail": "3층 상담실"
 *   }
 * }
 */
@Data
@Builder
public class ExpertJoinRequest {
    // 회원가입에 필요한 공통 속성 모음
    @Valid  // 유효성 검사를 위한 어노테이션
    private CommonInfo commonInfo;

    // 각 사용자의 특성에 따라 필요한 정보
    private ExpertInfo expertInfo;

    // 주소가 필요할 경우
    private AddressInfo addressInfo;

}
