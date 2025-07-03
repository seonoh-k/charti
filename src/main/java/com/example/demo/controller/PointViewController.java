package com.example.demo.controller;

import com.example.demo.enums.PointType;
import com.example.demo.users.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PointViewController {

    private final MemberRepository memberRepository;

    @GetMapping("/admin/point")
    public String pointManagement(
            @RequestParam(value = "id", required = false) Long memberId,
            Model model) {

        // 포인트 유형 드롭다운에 쓸 enum 목록
        model.addAttribute("pointTypes", PointType.values());

        // id 파라미터가 있으면, 해당 회원 이름(prefillName)과 ID(prefillId)를 모델에 담아두기
        if (memberId != null) {
            memberRepository.findById(memberId).ifPresent(m -> {
                model.addAttribute("prefillName", m.getName());
                model.addAttribute("prefillId", m.getId());
            });
        }
        return "admin/point/pointForm";
    }

}
