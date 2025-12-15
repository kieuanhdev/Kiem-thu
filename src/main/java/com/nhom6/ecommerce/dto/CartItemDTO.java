package com.nhom6.ecommerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemDTO {
    @NotNull(message = "ID sản phẩm không được để trống")
    private String productId;

    @Min(value = 1, message = "Số lượng mua tối thiểu là 1")
    private Integer quantity;

    @NotNull(message = "Client phải gửi giá đang hiển thị để đối chiếu")
    private BigDecimal clientPrice;
}