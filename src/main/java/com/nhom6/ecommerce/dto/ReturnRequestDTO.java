package com.nhom6.ecommerce.dto;

import com.nhom6.ecommerce.entity.ReturnRequest;
import lombok.Data;
import java.util.List;

@Data
public class ReturnRequestDTO {
    private Long orderId;
    private String productId;
    private int quantity;

    // Enum Lý do
    private ReturnRequest.ReturnReason reason;

    private String description;

    private List<String> proofImages;

    // --- THÊM TRƯỜNG NÀY ---
    private List<String> proofVideos;
    // -----------------------

    private ReturnRequest.RefundMethod refundMethod;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountName;
}