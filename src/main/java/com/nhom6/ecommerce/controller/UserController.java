package com.nhom6.ecommerce.controller;

import com.nhom6.ecommerce.dto.UserRegistrationDTO;
import com.nhom6.ecommerce.entity.User;
import com.nhom6.ecommerce.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        try {
            // Sửa đổi: Hứng kết quả trả về từ Service
            User newUser = userService.registerUser(registrationDTO);

            // Trả về toàn bộ thông tin User vừa tạo (Password đã bị @JsonIgnore ẩn đi)
            return ResponseEntity.ok(newUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}