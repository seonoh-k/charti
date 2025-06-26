package com.example.demo.fcm.dto;

import com.example.demo.enums.FcmCategory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Getter
@Setter
public class FcmHistorySearchDto {
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private FcmCategory category;

    private String senderName;
}