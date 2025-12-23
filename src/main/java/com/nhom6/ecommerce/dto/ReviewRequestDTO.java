package com.nhom6.ecommerce.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class ReviewRequestDTO {

    @NotBlank(message = "Erg3: Bạn cần đăng nhập để đánh giá sản phẩm")
    private String userId;

    @NotBlank(message = "Erg1: Không xác định được sản phẩm cần đánh giá")
    private String productId;

    @NotNull(message = "Erg6: Vui lòng chọn số sao đánh giá")
    @Min(value = 1, message = "Erg7: Số sao phải nằm trong khoảng từ 1 đến 5")
    @Max(value = 5, message = "Erg7: Số sao phải nằm trong khoảng từ 1 đến 5")
    private Integer rating;

    @Size(max = 500, message = "Erg8: Nội dung đánh giá không vượt quá 500 ký tự")
    private String content;

    // [Erg11] Kiểm tra số lượng file ngay tại DTO (hoặc trong Service)
    @Size(max = 5, message = "Erg11: Tối đa 5 ảnh cho mỗi đánh giá")
    private List<MultipartFile> images;
}