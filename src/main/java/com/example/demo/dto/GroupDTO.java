package com.example.demo.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupDTO {

    private Long id;

    private String groupName;
    private String groupEmail;
    private String groupPhoneNumber;

    private List<ChildDTO> children;

}