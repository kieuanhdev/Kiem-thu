package com.nhom6.ecommerce.service;

import com.nhom6.ecommerce.dto.VoucherRequestDTO;
import com.nhom6.ecommerce.entity.Product;
import com.nhom6.ecommerce.entity.Voucher;
import com.nhom6.ecommerce.repository.CategoryRepository;
import com.nhom6.ecommerce.repository.ProductRepository;
import com.nhom6.ecommerce.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class VoucherService {

    @Autowired private VoucherRepository voucherRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;

    /**
     * HÀM CHÍNH: TẠO VOUCHER
     * Luồng xử lý chính được đơn giản hóa tối đa, chỉ gọi các bước validate.
     */
    @Transactional
    public Voucher createVoucher(VoucherRequestDTO req) {
        // 1. Chuẩn hóa dữ liệu đầu vào (Trim, UpperCase...)
        String code = req.getCode().trim().toUpperCase();
        String name = req.getName().trim();

        // 2. Thực hiện các bước Validation theo thứ tự
        validateCode(code);
        validateName(name);
        validateDiscount(req);
        validateTime(req.getStartAt(), req.getEndAt());
        validateUsageLimit(req);
        validateScope(req);
        validateAudience(req);

        // 3. Mapping & Lưu
        return saveVoucher(req, code, name);
    }

    // =========================================================================
    // CÁC HÀM VALIDATION NHỎ (PRIVATE METHODS)
    // =========================================================================

    /**
     * 1. Validate Mã Voucher (Code)
     */
    private void validateCode(String code) {
        if (code.contains(" ") || code.contains("--")) {
            throw new RuntimeException("1E.4: Mã voucher không được chứa khoảng trắng hoặc '--'.");
        }
        if (voucherRepository.existsByCode(code)) {
            throw new RuntimeException("1E.5: Mã voucher đã tồn tại.");
        }
        if (code.startsWith("MKT") || code.startsWith("FLS") || code.startsWith("VNPAY")) {
            throw new RuntimeException("1E.6: Mã voucher xung đột với chương trình khác.");
        }
    }

    /**
     * 2. Validate Tên Voucher (Name)
     */
    private void validateName(String name) {
        if (Pattern.matches("^[0-9]+$", name)) {
            throw new RuntimeException("2E.4: Tên voucher không được chỉ gồm số.");
        }
        if (name.matches(".*[<>/'\"{}].*") || containsEmoji(name)) {
            throw new RuntimeException("2E.3: Tên voucher chứa ký tự không hợp lệ.");
        }
    }

    /**
     * 3 & 4. Validate Loại giảm giá & Giá trị (Discount Type & Value)
     */
    private void validateDiscount(VoucherRequestDTO req) {
        // [3E.3] Check vận chuyển
        if (req.getDiscountType() == Voucher.DiscountType.SHIPPING) {
            boolean shippingServiceSupport = true; // Giả lập
            if (!shippingServiceSupport) throw new RuntimeException("3E.3: Loại giảm giá vận chuyển không khả dụng.");
        }

        BigDecimal val = req.getDiscountValue();

        // [4E.1] Check Phần trăm
        if (req.getDiscountType() == Voucher.DiscountType.PERCENTAGE) {
            if (val.compareTo(BigDecimal.ONE) < 0 || val.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new RuntimeException("4E.1: Giá trị phần trăm phải từ 1 đến 100%.");
            }
        }
        // [4E.3 - 4E.5] Check Số tiền cố định
        else if (req.getDiscountType() == Voucher.DiscountType.FIXED_AMOUNT) {
            if (val.compareTo(BigDecimal.valueOf(1000)) < 0) {
                throw new RuntimeException("4E.3: Giá trị giảm tối thiểu là 1.000đ.");
            }
            if (val.compareTo(BigDecimal.valueOf(10000000)) > 0) {
                throw new RuntimeException("4E.4: Giá trị giảm tối đa là 10.000.000đ.");
            }
            if (val.compareTo(req.getMinOrderValue()) > 0) {
                throw new RuntimeException("4E.5: Giá trị giảm không được lớn hơn giá trị tối thiểu đơn hàng.");
            }
            if (val.stripTrailingZeros().scale() > 0) {
                throw new RuntimeException("Giá trị giảm phải là số nguyên, không được lẻ thập phân.");
            }
        }
    }

    /**
     * 6. Validate Thời gian (Time)
     */
    private void validateTime(LocalDateTime startAt, LocalDateTime endAt) {
        LocalDateTime now = LocalDateTime.now();
        if (startAt.isBefore(now.minusSeconds(60))) {
            throw new RuntimeException("6E.1: Ngày bắt đầu không hợp lệ (Quá khứ).");
        }
        if (!endAt.isAfter(startAt)) {
            throw new RuntimeException("6E.2: Ngày kết thúc phải lớn hơn ngày bắt đầu.");
        }
        if (ChronoUnit.YEARS.between(startAt, endAt) > 3) {
            throw new RuntimeException("6E.3: Thời gian áp dụng voucher không vượt quá 3 năm.");
        }
    }

    /**
     * 8. Validate Giới hạn sử dụng (Usage Limit)
     */
    private void validateUsageLimit(VoucherRequestDTO req) {
        int limitPerUser = req.getUsageLimitPerUser() != null ? req.getUsageLimitPerUser() : 1;

        if (limitPerUser > req.getUsageLimit()) {
            throw new RuntimeException("8E.2: Giới hạn người dùng không được vượt quá tổng số lượng phát hành.");
        }
        if (req.getAudienceType() == Voucher.AudienceType.NEW_USER && limitPerUser != 1) {
            throw new RuntimeException("8E.3: Voucher cho khách mới chỉ được dùng 1 lần.");
        }
    }

    /**
     * 9 & 11. Validate Phạm vi áp dụng (Scope)
     * Hàm này lại chia nhỏ tiếp thành 2 hàm con nữa để code gọn hơn.
     */
    private void validateScope(VoucherRequestDTO req) {
        List<String> scopeIds = req.getScopeIds();

        if (req.getScope() == Voucher.ScopeType.CATEGORY) {
            validateCategoryScope(scopeIds);
        } else if (req.getScope() == Voucher.ScopeType.PRODUCT) {
            validateProductScope(scopeIds, req.getDiscountType(), req.getDiscountValue());
        }
    }

    private void validateCategoryScope(List<String> scopeIds) {
        if (scopeIds == null || scopeIds.isEmpty())
            throw new RuntimeException("Danh sách danh mục không được để trống.");
        if (scopeIds.size() > 20)
            throw new RuntimeException("11E.5: Tối đa 20 danh mục được phép áp dụng.");

        for (String catIdStr : scopeIds) {
            try {
                Long catId = Long.parseLong(catIdStr);
                if (!categoryRepository.existsById(catId)) {
                    throw new RuntimeException("9E.3: Danh mục không tồn tại (ID: " + catId + ")");
                }
            } catch (NumberFormatException e) {
                throw new RuntimeException("ID danh mục không hợp lệ (Phải là số): " + catIdStr);
            }
        }
    }

    private void validateProductScope(List<String> scopeIds, Voucher.DiscountType type, BigDecimal val) {
        if (scopeIds == null || scopeIds.isEmpty())
            throw new RuntimeException("Danh sách sản phẩm không được để trống.");
        if (scopeIds.size() > 200)
            throw new RuntimeException("11E.12: Tối đa 200 sản phẩm được phép áp dụng.");

        for (String prodId : scopeIds) {
            Product p = productRepository.findById(prodId)
                    .orElseThrow(() -> new RuntimeException("9E.5: Sản phẩm không tồn tại (ID: " + prodId + ")"));

            if (!p.isActive())
                throw new RuntimeException("9E.6: Sản phẩm đã ngừng kinh doanh: " + p.getName());

            // Check giá trị giảm so với giá sản phẩm
            if (type == Voucher.DiscountType.FIXED_AMOUNT && val.compareTo(p.getSalePrice()) > 0) {
                throw new RuntimeException("11E.14: Giá trị giảm (" + val + ") lớn hơn giá bán sản phẩm " + p.getName());
            }
        }
    }

    /**
     * 12. Validate Đối tượng áp dụng (Audience)
     */
    private void validateAudience(VoucherRequestDTO req) {
        if (req.getAudienceType() == Voucher.AudienceType.MEMBER) {
            if (req.getMemberTierId() == null) {
                throw new RuntimeException("10E.2: Hạng thành viên không tồn tại.");
            }
        }
    }

    /**
     * Hàm phụ trợ: Mapping & Save
     */
    private Voucher saveVoucher(VoucherRequestDTO req, String code, String name) {
        Voucher voucher = new Voucher();
        voucher.setCode(code);
        voucher.setName(name);
        voucher.setDiscountType(req.getDiscountType());
        voucher.setDiscountValue(req.getDiscountValue());
        voucher.setMinOrderValue(req.getMinOrderValue());
        voucher.setStartAt(req.getStartAt());
        voucher.setEndAt(req.getEndAt());
        voucher.setUsageLimit(req.getUsageLimit());
        voucher.setUsageLimitPerUser(req.getUsageLimitPerUser() != null ? req.getUsageLimitPerUser() : 1);
        voucher.setScope(req.getScope());
        voucher.setScopeIds(req.getScopeIds());
        voucher.setAudienceType(req.getAudienceType() != null ? req.getAudienceType() : Voucher.AudienceType.ALL);
        voucher.setMemberTierId(req.getMemberTierId());

        // Mặc định ban đầu
        voucher.setUsedCount(0);
        voucher.setActive(true);

        return voucherRepository.save(voucher);
    }

    // Helper check Emoji
    private boolean containsEmoji(String source) {
        if (source == null) return false;
        return source.codePoints().anyMatch(
                c -> (c >= 0x1F600 && c <= 0x1F64F) || // Emoticons
                        (c >= 0x1F300 && c <= 0x1F5FF) || // Misc Symbols and Pictographs
                        (c >= 0x1F680 && c <= 0x1F6FF)    // Transport and Map
        );
    }
}