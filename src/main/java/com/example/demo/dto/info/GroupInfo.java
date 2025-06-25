package com.example.demo.dto.info;

import com.example.demo.enums.TargetGroup;
import lombok.Data;

@Data
public class GroupInfo {

    private Long groupId;
    private String targetGroup;
    private String groupEmail;
    private String groupName;
    private String groupPhoneNumber;

}