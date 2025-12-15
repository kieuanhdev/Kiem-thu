package com.nhom6.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "vouchers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1. Mã Voucher [cite: 2355-2370]
    @Column(nullable = false, unique = true, length = 20)
    private String code;

    // 2. Tên Voucher [cite: 2371-2379]
    @Column(nullable = false, length = 50)
    private String name;

    // 3. Loại giảm giá [cite: 2379-2387]
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;

    // 4. Giá trị giảm [cite: 2388-2413]
    @Column(name = "discount_value")
    private BigDecimal discountValue;

    // 5. Giá trị đơn hàng tối thiểu [cite: 2414-2423]
    @Column(name = "min_order_value")
    private BigDecimal minOrderValue;

    // 6 & 7. Thời gian áp dụng [cite: 2423-2435]
    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    // 8. Số lượng phát hành [cite: 2435-2442]
    @Column(name = "usage_limit")
    private Integer usageLimit;

    // 9. Giới hạn mỗi người dùng [cite: 2442-2449]
    @Column(name = "usage_limit_per_user")
    private Integer usageLimitPerUser = 1;

    // 14. Lượt đã dùng [cite: 2499-2505]
    @Column(name = "used_count")
    private Integer usedCount = 0;

    // 10. Phạm vi áp dụng [cite: 2449-2455]
    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false)
    private ScopeType scope;

    // 11. Danh sách ID áp dụng (Category ID hoặc Product ID) [cite: 2455-2475]
    @ElementCollection
    @CollectionTable(name = "voucher_scopes", joinColumns = @JoinColumn(name = "voucher_id"))
    @Column(name = "scope_id")
    private List<Long> scopeIds;

    // 13. Trạng thái hoạt động [cite: 2492-2498]
    @Column(name = "is_active")
    private boolean isActive = true;

    // Enum
    public enum DiscountType { PERCENTAGE, FIXED_AMOUNT, SHIPPING }
    public enum ScopeType { GLOBAL, CATEGORY, PRODUCT }
}