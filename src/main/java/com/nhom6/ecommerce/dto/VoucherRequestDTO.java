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

    @NotBlank(message = "1E.1: Vui lòng nhập mã voucher")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "1E.3: Mã chỉ chứa chữ in hoa, số và dấu gạch ngang")
    @Size(max = 20, message = "1E.2: Mã tối đa 20 ký tự")
    private String code;

    @NotBlank(message = "2E.1: Vui lòng nhập tên voucher")
    @Size(max = 50, message = "2E.2: Tên tối đa 50 ký tự")
    private String name;

    @NotNull(message = "3E.1: Vui lòng chọn loại giảm giá")
    private Voucher.DiscountType discountType;

    @NotNull(message = "Giá trị giảm là bắt buộc")
    @Min(value = 0, message = "Giá trị giảm không được âm")
    private BigDecimal discountValue;

    @NotNull(message = "5E.1: Vui lòng nhập giá trị đơn hàng tối thiểu")
    @Min(value = 1000, message = "5E.2: Giá trị tối thiểu phải từ 1.000đ")
    private BigDecimal minOrderValue;

    @NotNull(message = "Vui lòng chọn ngày bắt đầu")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) // <--- THÊM DÒNG NÀY
    private LocalDateTime startAt;

    @NotNull(message = "Vui lòng chọn ngày kết thúc")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) // <--- THÊM DÒNG NÀY
    private LocalDateTime endAt;

    @NotNull(message = "7E.1: Vui lòng nhập số lượng phát hành")
    @Max(value = 100000, message = "7E.2: Số lượng phát hành vượt giới hạn (100k)")
    private Integer usageLimit;

    @Min(value = 1, message = "8E.1: Giới hạn người dùng tối thiểu là 1")
    private Integer usageLimitPerUser;

    @NotNull(message = "9E.1: Vui lòng chọn phạm vi áp dụng")
    private Voucher.ScopeType scope;

    private List<Long> scopeIds; // Có thể rỗng nếu scope là GLOBAL
}