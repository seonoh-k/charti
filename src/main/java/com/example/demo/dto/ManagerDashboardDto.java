package com.example.demo.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ManagerDashboardDto {
    private String managerName;
    private String managerEmail;
    private String managerPhone;
    private String groupName;
    private String groupEmail;
    private long childCount;
}
