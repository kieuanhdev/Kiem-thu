package com.nhom6.ecommerce.dto;

import com.nhom6.ecommerce.entity.Order;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class OrderRequestDTO {
    // Giả lập lấy ID user từ Token (ở dự án thật sẽ lấy từ SecurityContext)
    @NotBlank(message = "Thiếu User ID")
    private String userId;

    @NotBlank(message = "1E.1: Họ tên không được bỏ trống")
    @Size(min = 2, max = 50, message = "1E.4: Tên từ 2-50 ký tự")
    private String recipientName;

    @NotBlank(message = "1E.1: Số điện thoại không được bỏ trống")
    @Pattern(regexp = "^0\\d{9}$", message = "1E.3: SĐT phải 10 số và bắt đầu bằng 0")
    private String phone;

    @NotBlank(message = "1E.1: Địa chỉ không được bỏ trống")
    @Size(min = 10, message = "1E.4: Địa chỉ quá ngắn (min 10)")
    private String address;

    @NotNull(message = "4E.1: Vui lòng chọn phương thức thanh toán")
    private Order.PaymentMethod paymentMethod;

    private String voucherCode; // Optional

    @NotNull(message = "2E.1: Giỏ hàng trống")
    @Size(min = 1, max = 50, message = "Giới hạn 50 loại sản phẩm/đơn")
    private List<CartItemDTO> items;
}