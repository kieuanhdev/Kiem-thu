package com.nhom6.ecommerce.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "vouchers")
@Data
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1. Mã Voucher
    @Column(nullable = false, unique = true, length = 20)
    private String code;

    // 2. Tên Voucher
    @Column(nullable = false, length = 50)
    private String name;

    // 3. Loại giảm giá
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;

    // 4. Giá trị giảm
    @Column(name = "discount_value")
    private BigDecimal discountValue;

    // 5. Giá trị đơn hàng tối thiểu
    @Column(name = "min_order_value")
    private BigDecimal minOrderValue;

    // 6. Thời gian áp dụng
    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    // 7 & 8. Số lượng
    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "usage_limit_per_user")
    private Integer usageLimitPerUser;

    // 9 & 10. Phạm vi áp dụng
    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false)
    private ScopeType scope;

    // 11. Danh sách ID áp dụng
    // Trong Voucher.java
    @ElementCollection
    @CollectionTable(name = "voucher_scopes", joinColumns = @JoinColumn(name = "voucher_id"))
    @Column(name = "scope_id")
    private List<String> scopeIds; // Đổi từ Long sang String

    // 12. Đối tượng áp dụng (Mới)
    @Enumerated(EnumType.STRING)
    @Column(name = "audience_type")
    private AudienceType audienceType = AudienceType.ALL;

    @Column(name = "member_tier_id")
    private Long memberTierId; // ID hạng thành viên nếu audience = MEMBER

    // 13. Trạng thái
    @Column(name = "is_active")
    private boolean isActive = true;

    // 14. Lượt đã dùng
    @Column(name = "used_count")
    private Integer usedCount = 0;

    // Enums
    public enum DiscountType { PERCENTAGE, FIXED_AMOUNT, SHIPPING }
    public enum ScopeType { GLOBAL, CATEGORY, PRODUCT }
    public enum AudienceType { ALL, NEW_USER, MEMBER } //
}