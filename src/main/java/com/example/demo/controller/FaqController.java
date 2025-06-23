package com.example.demo.controller;

import com.example.demo.dto.FaqCategoryDTO;
import com.example.demo.dto.FaqDTO;
import com.example.demo.entity.FAQ;
import com.example.demo.service.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class FaqController {
    private final FaqService faqService;

    @GetMapping("/faq")
    public String getFaqList(Model model) {
        List<FAQ> faqs = faqService.getList();

        Map<String, List<FaqDTO>> map = new HashMap<>();
        for(FAQ faq : faqs) {
            String category = faq.getCategory();
            if(!map.containsKey(category)) {
                map.put(category, new ArrayList<>());
            }
            map.get(category).add(new FaqDTO(faq.getQuestion(), faq.getAnswer()));
        }

        List<FaqCategoryDTO> faqList = new ArrayList<>();
        for(Map.Entry<String, List<FaqDTO>> entry : map.entrySet()) {
            FaqCategoryDTO dto = new FaqCategoryDTO(entry.getKey(), entry.getValue());
            faqList.add(dto);
        }

        model.addAttribute("faqList", faqList);
        return "faq";
    }
}
