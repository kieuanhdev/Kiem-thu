package com.nhom6.ecommerce.controller;

import com.nhom6.ecommerce.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/orders")
public class OrderWebController {

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping
    public String listOrders(Model model) {
        // Lấy danh sách đơn, sắp xếp mới nhất lên đầu
        model.addAttribute("orders", orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")));
        return "order-list";
    }
}