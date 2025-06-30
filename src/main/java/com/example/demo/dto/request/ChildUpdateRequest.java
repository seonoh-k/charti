package com.example.demo.dto.request;


import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;


@Data
public class ChildUpdateRequest {
    private Long id;
    private String name;
    private String nickname;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
    private String gender;
    private String height;
    private String weight;
    private Integer birthOrder;
    private Long groupId;



}

