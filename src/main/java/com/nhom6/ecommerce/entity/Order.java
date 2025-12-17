package com.nhom6.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người đặt hàng (Liên kết với bảng User) [cite: 63]
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Thông tin giao hàng [cite: 69-90]
    @Column(name = "recipient_name", nullable = false)
    private String recipientName;

    @Column(nullable = false, length = 10)
    private String phone;

    @Column(nullable = false)
    private String address;

    // Phương thức thanh toán [cite: 110-114]
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    // Tài chính [cite: 144]
    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount; // Tổng tiền cuối cùng (sau khi trừ voucher)

    @Column(name = "voucher_code")
    private String voucherCode;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    // Trạng thái đơn hàng
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Quan hệ với chi tiết đơn hàng
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate; // Ngày giao hàng thành công

    public enum PaymentMethod { COD, BANKING, MOMO }
    // Trong file Order.java của bạn
    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        SHIPPING,
        DELIVERED,
        COMPLETED,
        CANCELLED,

        // --- THÊM TRẠNG THÁI NÀY ---
        RETURN_REQUESTED // Đã yêu cầu trả hàng/hoàn tiền
    }
}