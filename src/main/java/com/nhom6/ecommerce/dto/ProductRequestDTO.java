package com.nhom6.ecommerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequestDTO {

    @NotBlank(message = "SKU không được để trống")
    private String sku;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    @NotNull(message = "Thương hiệu là bắt buộc")
    private Long brandId;

    @NotNull(message = "Phải chọn ít nhất một danh mục")
    private List<Long> categoryIds;

    @NotNull(message = "Giá bán lẻ là bắt buộc")
    @Min(value = 0, message = "Giá bán không được âm")
    private BigDecimal salePrice;

    private BigDecimal originalPrice;
    private BigDecimal importPrice;

    @Min(value = 0)
    private Integer stockQuantity;

    private String thumbnail;
    private List<String> gallery;
    private String description;
    private List<String> tags;
}