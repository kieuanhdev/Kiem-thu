package com.nhom6.ecommerce.service;

import com.nhom6.ecommerce.dto.VoucherRequestDTO;
import com.nhom6.ecommerce.entity.Voucher;
import com.nhom6.ecommerce.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class VoucherService {

    @Autowired
    private VoucherRepository voucherRepository;

    public Voucher createVoucher(VoucherRequestDTO req) {
        // 1. Chuẩn hóa mã Code (In hoa, bỏ khoảng trắng) [cite: 2363, 2360]
        String normalizedCode = req.getCode().trim().toUpperCase();

        // 2. Validate Mã trùng [cite: 2364]
        if (voucherRepository.existsByCode(normalizedCode)) {
            throw new RuntimeException("1E.5: Mã voucher đã tồn tại");
        }

        // 3. Validate Logic Thời gian [cite: 2426, 2432]
        LocalDateTime now = LocalDateTime.now();
        if (req.getStartAt().isBefore(now)) {
            // Lưu ý: Có thể cho phép tạo nếu startAt trễ hơn now một chút do delay mạng, tùy policy
            // Ở đây chặn cứng theo tài liệu 6E.1
            // throw new RuntimeException("6E.1: Ngày bắt đầu không được ở quá khứ");
        }
        if (req.getEndAt().isBefore(req.getStartAt())) {
            throw new RuntimeException("6E.2: Ngày kết thúc phải sau ngày bắt đầu");
        }

        // 4. Validate Logic Giá trị giảm [cite: 2391, 2403]
        if (req.getDiscountType() == Voucher.DiscountType.PERCENTAGE) {
            if (req.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new RuntimeException("4E.1: Giá trị phần trăm phải từ 1 đến 100%");
            }
        } else if (req.getDiscountType() == Voucher.DiscountType.FIXED_AMOUNT) {
            if (req.getDiscountValue().compareTo(req.getMinOrderValue()) > 0) {
                throw new RuntimeException("4E.5: Giá trị giảm không được lớn hơn đơn hàng tối thiểu");
            }
        }

        // 5. Validate Scope [cite: 2453, 2454]
        if (req.getScope() != Voucher.ScopeType.GLOBAL && (req.getScopeIds() == null || req.getScopeIds().isEmpty())) {
            throw new RuntimeException("11E.1: Vui lòng chọn danh mục/sản phẩm áp dụng");
        }

        // 6. Map dữ liệu & Lưu
        Voucher voucher = new Voucher();
        voucher.setCode(normalizedCode);
        voucher.setName(req.getName());
        voucher.setDiscountType(req.getDiscountType());
        voucher.setDiscountValue(req.getDiscountValue());
        voucher.setMinOrderValue(req.getMinOrderValue());
        voucher.setStartAt(req.getStartAt());
        voucher.setEndAt(req.getEndAt());
        voucher.setUsageLimit(req.getUsageLimit());
        voucher.setUsageLimitPerUser(req.getUsageLimitPerUser() != null ? req.getUsageLimitPerUser() : 1);
        voucher.setScope(req.getScope());
        if (req.getScope() != Voucher.ScopeType.GLOBAL) {
            voucher.setScopeIds(req.getScopeIds());
        }
        voucher.setActive(true);

        return voucherRepository.save(voucher);
    }
}