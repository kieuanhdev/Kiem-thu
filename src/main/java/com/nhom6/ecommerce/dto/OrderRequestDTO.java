package com.nhom6.ecommerce.dto;

import com.nhom6.ecommerce.entity.Order;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class OrderRequestDTO {
    @NotBlank(message = "Thiếu User ID")
    private String userId;

    // [1E.1, 1E.2] Validate Họ tên chặt chẽ
    @NotBlank(message = "1E.1: Vui lòng nhập đầy đủ thông tin giao hàng.")
    @Size(min = 2, max = 50, message = "1E.2: Họ tên phải từ 2-50 ký tự.")
    @Pattern(regexp = "^[\\p{L} ]+$", message = "1E.2: Họ tên người nhận không hợp lệ (Chỉ chứa chữ cái).")
    private String recipientName;

    // [1E.3] Validate SĐT
    @NotBlank(message = "1E.1: Vui lòng nhập đầy đủ thông tin giao hàng.")
    @Pattern(regexp = "^0\\d{9}$", message = "1E.3: Số điện thoại không hợp lệ (Phải là 10 số, bắt đầu bằng 0).")
    private String phone;

    // [1E.4] Validate Địa chỉ
    @NotBlank(message = "1E.1: Vui lòng nhập đầy đủ thông tin giao hàng.")
    @Size(min = 10, max = 255, message = "1E.4: Địa chỉ quá ngắn (min 10) hoặc quá dài (max 255).")
    private String address;

    // [4E.1] Validate Thanh toán
    @NotNull(message = "4E.1: Vui lòng chọn phương thức thanh toán.")
    private Order.PaymentMethod paymentMethod;

    private String voucherCode;

    // [2E.1] Validate Giỏ hàng
    @NotNull(message = "2E.1: Giỏ hàng trống.")
    @Size(min = 1, message = "2E.1: Giỏ hàng trống.")
    @Size(max = 50, message = "Danh sách sản phẩm không được vượt quá 50 loại.")
    private List<CartItemDTO> items;
}