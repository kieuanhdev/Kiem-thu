package com.nhom6.ecommerce.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class ReviewRequestDTO {

    @NotBlank(message = "Erg3: Bạn cần đăng nhập để đánh giá")
    private String userId;

    @NotBlank(message = "Erg1: Không xác định được sản phẩm cần đánh giá")
    private String productId;

    @NotNull(message = "Erg6: Vui lòng chọn số sao đánh giá")
    @Min(value = 1, message = "Erg7: Số sao phải từ 1 đến 5")
    @Max(value = 5, message = "Erg7: Số sao phải từ 1 đến 5")
    private Integer rating;

    @Size(max = 500, message = "Erg8: Nội dung đánh giá không vượt quá 500 ký tự")
    private String content;

    @Size(max = 5, message = "Erg11: Tối đa 5 ảnh cho mỗi đánh giá")
    private List<String> images;
}