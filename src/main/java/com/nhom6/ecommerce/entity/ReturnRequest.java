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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1. Ngữ cảnh: Thuộc đơn hàng nào
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 2. Sản phẩm muốn trả
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    // Số lượng trả
    @Column(name = "quantity")
    private Integer quantity;

    // 3. Lý do & Minh chứng
    @Enumerated(EnumType.STRING)
    private ReturnReason reason;

    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả chi tiết

    @ElementCollection
    @CollectionTable(name = "return_proofs", joinColumns = @JoinColumn(name = "return_id"))
    @Column(name = "media_url")
    private List<String> proofImages; // Ảnh/Video bằng chứng

    // 4. Phương thức hoàn tiền
    @Enumerated(EnumType.STRING)
    private RefundMethod refundMethod;

    // 5. Thông tin ngân hàng (nếu hoàn qua Bank)
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountName;

    @Enumerated(EnumType.STRING)
    private ReturnStatus status = ReturnStatus.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Enums
    public enum ReturnReason { MISSING_ITEM, DAMAGED, WRONG_ITEM, CHANGED_MIND }
    public enum RefundMethod { ORIGINAL_SOURCE, BANK_TRANSFER, WALLET }
    public enum ReturnStatus { PENDING, APPROVED, REJECTED, REFUNDED }
}