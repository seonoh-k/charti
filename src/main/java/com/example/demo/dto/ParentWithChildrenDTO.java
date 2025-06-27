package com.example.demo.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentWithChildrenDTO {
    private Long parentId;
    private String parentName;
    private String parentPhone;
    private List<ChildSimpleDTO> children; // 자녀 여러 명!
}