package com.example.demo.users.controller;

import com.example.demo.dto.ExpertDTO;
import com.example.demo.dto.ManagerDTO;
import com.example.demo.dto.MemberDTO;
import com.example.demo.dto.paging.PagingRequest;
import com.example.demo.dto.paging.PagingResultDTO;
import com.example.demo.users.entity.Expert;
import com.example.demo.users.entity.Member;
import com.example.demo.users.entity.Role;
import com.example.demo.users.entity.Users;
import com.example.demo.users.exception.UserNotFoundException;
import com.example.demo.users.service.ExpertService;
import com.example.demo.users.service.ManagerService;
import com.example.demo.users.service.MemberService;
import com.example.demo.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Log4j2
public class AdminController {

    private final ExpertService expertService;
    private final MemberService memberService;
    private final ManagerService managerService;
    private final UserService userService;

    @GetMapping("/admin")
    public String showAdminPage() {
        log.info("[GET] 👨‍💼 request Admin Page");
        return "admin";
    }
    @GetMapping("/admin/main")
    public String showAdminMainPage(Model model) {
        log.info("[GET] 👨‍💼 request Admin Main Page");

        Sort sort = Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(0, 5, sort);

        List<ManagerDTO> managerDTOList = managerService.getManagerList(pageable);
        List<ExpertDTO> expertDTOList = expertService.getExpertList(pageable);
        List<MemberDTO> memberDTOList = userService.getMemberList(pageable);
        // List<MemberDTO> memberDTOList = memberService.getMemberList(pageable);

        model.addAttribute("managerDTOList",managerDTOList);
        model.addAttribute("expertDTOList",expertDTOList);
        model.addAttribute("memberDTOList",memberDTOList);

        return "admin/main";
    }

    @GetMapping("/admin/member/all")
    public String showAdminMemberListPage(@ModelAttribute PagingRequest pagingRequest, Model model){
        Pageable pageable = pagingRequest.toPageable();
        PagingResultDTO<MemberDTO, Users> result = userService.getMemberListWithPaging(pageable);

        model.addAttribute("result",result);
        return "admin/member/memberList"; // 뷰 파일
    }
    @GetMapping("/admin/member/{id:[0-9]+}")
    public String showAdminMemberUpdatePage(@ModelAttribute PagingRequest pagingRequest,
                                            @PathVariable Long id,
                                            RedirectAttributes redirectAttribute,
                                            Model model){
        MemberDTO memberDTO;
        try{
            memberDTO = userService.findById(id);

        } catch (UserNotFoundException userNotFoundException){
            redirectAttribute.addAttribute("page",pagingRequest.getPage());
            redirectAttribute.addAttribute("size",pagingRequest.getSize());
            redirectAttribute.addAttribute("sort",pagingRequest.getSort());
            redirectAttribute.addAttribute("direction",pagingRequest.getDirection());
            return "redirect:/admin/member/all";
        }

        model.addAttribute("memberDTO",memberDTO);

        /**
         * 유저 이름.
         */
        return "/admin/member/updateForm";
    }




}