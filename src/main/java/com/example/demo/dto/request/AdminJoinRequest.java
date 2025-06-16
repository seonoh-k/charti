package com.example.demo.dto.request;

import com.example.demo.dto.info.CommonInfo;
import com.example.demo.users.entity.Admin;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminJoinRequest {

    // 공통
    private CommonInfo commonInfo;

    // 사용자 특성 별로 요구되는 정보

    // 주소
}
