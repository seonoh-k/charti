package com.example.demo.dto;

import com.example.demo.entity.Group;
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
    private String targetGroup;

    private List<ChildDTO> children;

    public GroupDTO(Group group) {
        this.id = group.getId();
        this.groupName = group.getGroupName();
    }
}