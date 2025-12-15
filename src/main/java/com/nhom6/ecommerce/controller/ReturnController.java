package com.nhom6.ecommerce.controller;

import com.nhom6.ecommerce.dto.ReturnRequestDTO;
import com.nhom6.ecommerce.service.ReturnService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/returns")
public class ReturnController {

    @Autowired
    private ReturnService returnService;

    @PostMapping
    public ResponseEntity<?> createReturnRequest(@Valid @RequestBody ReturnRequestDTO req) {
        try {
            return ResponseEntity.ok(returnService.createReturnRequest(req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}