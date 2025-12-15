package com.nhom6.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nhom6.ecommerce.entity.Voucher;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VoucherRequestDTO {

    // 1. Mã Voucher
    @NotBlank(message = "1E.1: Vui lòng nhập mã voucher.")
    @Size(max = 20, message = "1E.2: Mã voucher không được vượt quá 20 ký tự.")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "1E.3: Mã voucher chỉ được chứa chữ in hoa, số và dấu '-'.")
    private String code;

    // 2. Tên Voucher
    @NotBlank(message = "2E.1: Vui lòng nhập tên voucher.")
    @Size(max = 50, message = "2E.2: Tên voucher không được vượt quá 50 ký tự.")
    private String name;

    // 3. Loại giảm giá
    @NotNull(message = "3E.1: Vui lòng chọn loại giảm giá.")
    private Voucher.DiscountType discountType;

    // 4. Giá trị giảm (Validate chi tiết ở Service)
    @NotNull(message = "Vui lòng nhập giá trị giảm")
    private BigDecimal discountValue;

    // 5. Giá trị tối thiểu
    @NotNull(message = "5E.1: Vui lòng nhập giá trị đơn hàng tối thiểu.")
    @Min(value = 1000, message = "5E.2: Giá trị tối thiểu phải từ 1.000đ.")
    @Max(value = 10000000, message = "5E.3: Giá trị tối thiểu không vượt quá 10.000.000đ.")
    private BigDecimal minOrderValue;

    // 6. Thời gian
    @NotNull(message = "Vui lòng chọn ngày bắt đầu")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startAt;

    @NotNull(message = "Vui lòng chọn ngày kết thúc")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endAt;

    // 7. Số lượng
    @NotNull(message = "7E.1: Vui lòng nhập số lượng phát hành.")
    @Max(value = 100000, message = "7E.2: Số lượng phát hành vượt giới hạn cho phép (100k).")
    @Min(value = 1, message = "7E.3: Không thể đặt số lượng phát hành bằng 0 (trừ khi unlimited).")
    private Integer usageLimit;

    // 8. Giới hạn người dùng
    @Min(value = 1, message = "8E.1: Giới hạn theo người dùng không hợp lệ.")
    @Max(value = 100, message = "Giới hạn người dùng tối đa là 100.")
    private Integer usageLimitPerUser;

    // 9. Phạm vi
    @NotNull(message = "9E.1: Vui lòng chọn phạm vi áp dụng.")
    private Voucher.ScopeType scope;

    // 11. List ID
    private List<String> scopeIds;

    // 12. Đối tượng
    private Voucher.AudienceType audienceType;
    private Long memberTierId;
}