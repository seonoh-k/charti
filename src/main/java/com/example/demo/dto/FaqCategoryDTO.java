package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
public class FaqCategoryDTO {
    private String category;
    private List<FaqDTO> faqs;
}
