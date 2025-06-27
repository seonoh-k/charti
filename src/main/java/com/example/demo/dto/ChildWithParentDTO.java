package com.example.demo.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChildWithParentDTO {
    private Long childId;
    private String childName;
    private String gender;
    private String birthday;
    private Long parentId;
    private String parentName;
    private String parentPhone;
    private String nickname;
    private String height;
    private String weight;
    private Integer birthOrder;
    private Boolean riskGroup;

    // 반드시 아래 생성자 추가!
    public ChildWithParentDTO(
            Long childId,
            String childName,
            String gender,
            String birthday,
            String nickname,
            String height,
            String weight,
            Integer birthOrder,
            Boolean riskGroup,
            Long parentId,
            String parentName,
            String parentPhone
    ) {
        this.childId = childId;
        this.childName = childName;
        this.gender = gender;
        this.birthday = birthday;
        this.nickname = nickname;
        this.height = height;
        this.weight = weight;
        this.birthOrder = birthOrder;
        this.riskGroup = riskGroup;
        this.parentId = parentId;
        this.parentName = parentName;
        this.parentPhone = parentPhone;
    }
}