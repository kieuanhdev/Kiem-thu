package com.nhom6.ecommerce.dto;

import com.nhom6.ecommerce.entity.ReturnRequest; // <--- DÒNG QUAN TRỌNG
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class ReturnRequestDTO {
    @NotNull(message = "Thiếu mã đơn hàng")
    private Long orderId;

    @NotBlank(message = "Thiếu ID sản phẩm")
    private String productId;

    @Min(value = 1, message = "9E.4: Số lượng trả tối thiểu là 1")
    private Integer quantity;

    @NotNull(message = "Vui lòng chọn lý do trả hàng")
    private ReturnRequest.ReturnReason reason; // Cần import ReturnRequest mới hiểu dòng này

    @Size(min = 20, max = 500, message = "9E.5: Vui lòng nhập chi tiết lý do (tối thiểu 20 ký tự)")
    private String description;

    @Size(min = 1, max = 5, message = "9E.7: Vui lòng tải lên ít nhất 1 hình ảnh/video minh chứng")
    private List<String> proofImages;

    @NotNull(message = "Vui lòng chọn phương thức hoàn tiền")
    private ReturnRequest.RefundMethod refundMethod; // Cần import ReturnRequest

    private String bankName;

    @Size(min = 6, max = 20, message = "9E.10: Số tài khoản ngân hàng không hợp lệ")
    private String bankAccountNumber;

    private String bankAccountName;
}