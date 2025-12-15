package com.nhom6.ecommerce.controller;

import com.nhom6.ecommerce.dto.VoucherRequestDTO;
import com.nhom6.ecommerce.service.VoucherService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    @PostMapping
    public ResponseEntity<?> createVoucher(@Valid @RequestBody VoucherRequestDTO req) {
        try {
            return ResponseEntity.ok(voucherService.createVoucher(req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}