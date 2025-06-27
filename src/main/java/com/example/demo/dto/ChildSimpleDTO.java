package com.example.demo.dto;

import lombok.*;

import java.time.LocalDate;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChildSimpleDTO {
    private Long childId;
    private String childName;
    private String gender;
    private String birthday;
    private String nickname;
    private String height;
    private String weight;
    private Integer birthOrder;
    private Boolean riskGroup;


    public String getBirthdayDateOnly() {
        if (this.birthday == null) return "";
        // 최소 10자리면 yyyy-MM-dd만 추출
        return this.birthday.length() >= 10
                ? this.birthday.substring(0, 10)
                : this.birthday;
    }

}

