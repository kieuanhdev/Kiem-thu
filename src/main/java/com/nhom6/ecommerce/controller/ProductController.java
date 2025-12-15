package com.nhom6.ecommerce.controller;

import com.nhom6.ecommerce.dto.ProductRequestDTO;
import com.nhom6.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductRequestDTO req) {
        try {
            return ResponseEntity.ok(productService.createProduct(req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}