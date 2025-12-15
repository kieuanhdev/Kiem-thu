package com.nhom6.ecommerce.controller;

import com.nhom6.ecommerce.dto.OrderRequestDTO;
import com.nhom6.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequestDTO req) {
        try {
            return ResponseEntity.ok(orderService.createOrder(req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}