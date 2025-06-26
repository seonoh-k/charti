package com.example.demo.matching.dto;

import com.example.demo.enums.SurveyCategory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class MatchingRequestDto {
    private Long childId;
    private List<Long> answerIds = new ArrayList<>();
    private SurveyCategory category;
    private String title;
    private String content;

    // 이미지 업로드
    private MultipartFile image;
}
