package com.nhom6.ecommerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class DashboardController {

    @GetMapping("/dashboard")
    public String showDashboard() {
        return "admin-dashboard"; // Trả về file admin-dashboard.html
    }
}