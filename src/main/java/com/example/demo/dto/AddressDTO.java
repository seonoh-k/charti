package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

    private Long id;
    private String zipNum;
    private String sido;
    private String gugun;
    private String dong;
    private String bunji;
    private String addressDetail;

    // 상세주소 없는 생성자
    public AddressDTO(Long id, String zipNum, String sido, String gugun, String dong, String bunji) {
        this.id = id;
        this.zipNum = zipNum;
        this.sido = sido;
        this.gugun = gugun;
        this.dong = dong;
        this.bunji = bunji;
    }

}
