package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressDTO {

    private Long id;
    private String zipNum;
    private String sido;
    private String gugun;
    private String dong;
    private String bunji;
    private String addressDetail;

}
