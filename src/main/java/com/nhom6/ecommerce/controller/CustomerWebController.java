package com.nhom6.ecommerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomerWebController {

    @GetMapping("/profile")
    public String showProfilePage() {
        return "profile"; // Trả về file profile.html
    }
}