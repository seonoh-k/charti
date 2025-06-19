package com.example.demo.controller;

import com.example.demo.enums.PointType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PointViewController {


    @GetMapping("/admin/point")
    public String getAdminPointPage(Model model) {
        model.addAttribute("pointTypes", PointType.values());
        return "admin/point/pointForm";
    }


}
