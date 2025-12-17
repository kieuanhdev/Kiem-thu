package com.nhom6.ecommerce.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "return_requests")
@Data
public class ReturnRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;

    @Enumerated(EnumType.STRING)
    private ReturnReason reason;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Lưu danh sách ảnh/video. Trong DB thực tế nên dùng @ElementCollection hoặc convert sang JSON String
    @ElementCollection
    private List<String> proofImages;

    // --- THÊM FIELD NÀY ---
    @ElementCollection
    private List<String> proofVideos;
    // ----------------------

    @Enumerated(EnumType.STRING)
    private RefundMethod refundMethod;

    @Enumerated(EnumType.STRING)
    private ReturnStatus status;

    // Thông tin ngân hàng
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountName;

    private LocalDateTime createdAt;

    // --- ĐỊNH NGHĨA CÁC ENUM ---

    public enum ReturnReason {
        MISSING_ITEM,   // Thiếu hàng
        DAMAGED,        // Hư hỏng
        WRONG_ITEM,     // Sai hàng
        // --- THÊM 2 CÁI NÀY ---
        NOT_SATISFIED,  // Không ưng ý
        OTHER           // Khác
    }

    public enum RefundMethod {
        BANK_TRANSFER,  // Chuyển khoản
        // --- THÊM CÁI NÀY ---
        ORIGINAL_METHOD // Hoàn về nguồn (Ví, Thẻ)
    }

    public enum ReturnStatus {
        PENDING, APPROVED, REJECTED, REFUNDED
    }
}