package com.example.demo.dto.info;

import lombok.Builder;
import lombok.Data;

@Data
public class ExpertInfo {

    private String major;
    private String license;
    private String career;
    private Long addressId;

}
