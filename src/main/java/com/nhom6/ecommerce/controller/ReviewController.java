package com.nhom6.ecommerce.controller;

import com.nhom6.ecommerce.dto.ReviewRequestDTO;
import com.nhom6.ecommerce.entity.Order;
import com.nhom6.ecommerce.repository.OrderRepository;
import com.nhom6.ecommerce.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ReviewController {

    @Autowired private ReviewService reviewService;
    @Autowired private OrderRepository orderRepository; // Inject tạm để làm tool test

    // 1. API Tạo đánh giá
    @PostMapping("/reviews")
    public ResponseEntity<?> createReview(@Valid @RequestBody ReviewRequestDTO req) {
        try {
            return ResponseEntity.ok(reviewService.createReview(req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 2. API TOOL (Chỉ dùng để Test): Chuyển trạng thái đơn hàng
    // Để bạn có thể biến đơn hàng vừa mua thành "Đã giao" -> Mới đánh giá được
    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId, @RequestParam Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));
        order.setStatus(status);
        orderRepository.save(order);
        return ResponseEntity.ok("Đã cập nhật trạng thái đơn hàng thành: " + status);
    }
}