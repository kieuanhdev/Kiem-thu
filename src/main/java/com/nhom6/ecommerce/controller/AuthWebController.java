package com.nhom6.ecommerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthWebController {

    // Trang đăng nhập
    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // Trả về file login.html
    }

    // Trang đăng ký (nếu cần)
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register"; // Trả về file register.html
    }
}