package com.example.demo.controller;

import com.example.demo.dto.QnaDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.Qna;
import com.example.demo.entity.QnaAnswer;
import com.example.demo.enums.QnaCategory;
import com.example.demo.service.QnaService;
import com.example.demo.users.entity.Role;
import com.example.demo.users.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class QnaController {

    private final AuthService authService;
    private final QnaService qnaService;

    @GetMapping("/qna")
    public String qna(@RequestParam(value = "page", defaultValue = "1", required=false) int page,
                      @RequestParam(value = "category", required=false) QnaCategory category,
                      Model model) {

        Page<Qna> qnaPage = qnaService.getPagedList(null, category, page-1);

        List<QnaDTO> qnaList = getQnaList(qnaPage);

        model.addAttribute("category", QnaCategory.values());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", qnaPage.getTotalPages());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("qnaList", qnaList);
        return "qnaList";
    }

    @GetMapping("/admin/qna")
    public String adminQna(@RequestParam(value = "page", defaultValue = "1", required=false) int page,
                           @RequestParam(value = "category", required=false) QnaCategory category,
                           Model model) {

        UserDTO userDTO = authService.getLoginUser();
        boolean isAdmin = userDTO.getRole().equals(Role.ROLE_ADMIN.getKey());

        if(!isAdmin) {
            return "redirect:/qna";
        }

        Page<Qna> qnaPage = qnaService.getAdminPagedList(category, page-1);

        List<QnaDTO> qnaList = getQnaList(qnaPage);

        model.addAttribute("category", QnaCategory.values());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", qnaPage.getTotalPages());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("qnaList", qnaList);
        return "admin/adminQnaList";
    }

    public List<QnaDTO> getQnaList(Page<Qna> qnaDTOPage) {
        List<QnaDTO> qnaDTOList = new ArrayList<>();
        for(Qna qna : qnaDTOPage) {
            qnaDTOList.add(new QnaDTO(qna));
        }
        return qnaDTOList;
    }

    @GetMapping("qna/create")
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
            return "redirect:/qna";
        }

        if(qna.getAnswer() != null) {
            QnaAnswer ans = qna.getAnswer();
            model.addAttribute("ans", ans);
        }

        model.addAttribute("category", QnaCategory.values());
        model.addAttribute("qna", new QnaDTO(qna));
        model.addAttribute("isOwner", isOwner);

        return "qna";
    }

    @GetMapping("/admin/qna/{id}")
    public String getAdminQnaDetail(@PathVariable Long id, Model model) {

        Qna qna = qnaService.get(id);

        if(qna.getAnswer() != null) {
            QnaAnswer ans = qna.getAnswer();
            model.addAttribute("ans", ans);
        }

        model.addAttribute("category", QnaCategory.values());
        model.addAttribute("qna", new QnaDTO(qna));

        return "admin/adminQna";
    }

    @GetMapping("/qnaList")
    public String getQnaList(@RequestParam(value = "page", defaultValue = "1", required=false) int page,
                             @RequestParam(value = "category", required=false) QnaCategory category,
                             Model model) {
        UserDTO user = authService.getLoginUser();
        Page<Qna> qnaPage = qnaService.getPagedList(user.getId(), null, page-1);

        List<QnaDTO> qnaList = getQnaList(qnaPage);

        if(category != null) {
            model.addAttribute("category", QnaCategory.values());
        }
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", qnaPage.getTotalPages());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("qnaList", qnaList);

        if(user.getRole().equals(Role.ROLE_MEMBER.getKey())) {
            return "personalQnaList";
        }else if(user.getRole().equals(Role.ROLE_EXPERT.getKey())) {
            return "/admin/expert/expertPersonalQnaList";
        }else {
            return "/admin/manager/managerPersonalQnaList";
        }
    }

    @GetMapping("/qnaList/{id}")
    public String getPersonalQnaDetail(@PathVariable Long id, Model model) {
        UserDTO user = authService.getLoginUser();
        Qna qna = qnaService.get(id);

        if(qna.getAnswer() != null) {
            QnaAnswer ans = qna.getAnswer();
            model.addAttribute("ans", ans);
        }

        model.addAttribute("category", QnaCategory.values());
        model.addAttribute("qna", new QnaDTO(qna));

        if(user.getRole().equals(Role.ROLE_MEMBER.getKey())) {
            return "personalQna";
        }else if(user.getRole().equals(Role.ROLE_EXPERT.getKey())) {
            return "/admin/expert/expertPersonalQna";
        }else {
            return "/admin/manager/managerPersonalQna";
        }

    }

}
