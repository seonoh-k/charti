package com.example.demo.dto.request;

import com.example.demo.dto.AddressDTO;
import com.example.demo.dto.info.AddressInfo;
import com.example.demo.dto.info.CommonInfo;
import com.example.demo.dto.info.MemberInfo;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberJoinRequest {

    // 회원가입에 필요한 공통 속성 모음
    @Valid  // 유효성 검사를 위한 어노테이션
    private CommonInfo commonInfo;


    private MemberInfo memberInfo;

    // 주소가 필요할 경우
    private AddressInfo addressInfo;

}
