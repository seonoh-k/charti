package com.example.demo.dto;

import com.example.demo.enums.PointType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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

    @JsonProperty("created")
    public String getCreatedFormatted() {
        if (createdAt == null) return "-";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return createdAt.format(formatter);
    }
}
