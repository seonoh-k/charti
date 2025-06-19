package com.example.demo.dto;

import com.example.demo.enums.PointType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class PointHistoryView {
    private int beforePoint;
    private int afterPoint;
    private int changeAmount;
    private String description;
    private LocalDateTime createdAt;
    private PointType pointType;

    @JsonProperty("pointTypeLabel")
    public String getPointTypeLabel() {
        return pointType != null ? pointType.getLabel() : "-";
    }
}
