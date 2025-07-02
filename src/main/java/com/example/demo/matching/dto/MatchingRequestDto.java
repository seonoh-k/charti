package com.example.demo.matching.dto;

import com.example.demo.enums.SurveyCategory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class MatchingRequestDto {

    public enum AnswerType { SPECIAL, GROUP }

    private Long childId;
    private List<Long> answerIds = new ArrayList<>();
    private SurveyCategory category;
    private String title;
    private String content;
    private AnswerType type;

    // 이미지 업로드
    private MultipartFile image;
}
