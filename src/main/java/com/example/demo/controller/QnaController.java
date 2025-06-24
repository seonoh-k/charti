package com.example.demo.controller;

import com.example.demo.dto.QnaDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.Qna;
import com.example.demo.enums.QnaCategory;
import com.example.demo.service.QnaService;
import com.example.demo.users.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class QnaController {

    private final AuthService authService;
    private final QnaService qnaService;

    @GetMapping("/qna")
    public String qna(Model model) {
        List<QnaDTO> qnaList = qnaService.getPagedList(0);

        model.addAttribute("qnaList", qnaList);
        return "qnaList";
    }

    @GetMapping("qns/create")
    public String createQna(Model model) {
        model.addAttribute("category", QnaCategory.values());

        return "qnaForm";
    }

    @GetMapping("/qna/{id}")
    public String getQnaDetail(@PathVariable Long id, Model model) {
        UserDTO userDTO = authService.getLoginUser();
        Qna qna = qnaService.get(id);

        boolean isOwner = userDTO.getId().equals(qna.getUsers().getId());

        if(!isOwner && !qna.isPublic()) {
            return "redirect:/qnaList";
        }

        model.addAttribute("qna", qna);

        return "qna";
    }

}
