package com.nhom6.ecommerce.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {

    // =========================================================================
    // 1. NHÓM ĐỊNH DANH & CƠ BẢN
    // =========================================================================

    // [1E.x] SKU: Bắt buộc, max 50, chỉ chứa chữ, số, -, _
    @NotBlank(message = "Mã SKU không được để trống")
    @Size(max = 50, message = "Mã SKU tối đa 50 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "SKU chỉ được chứa chữ cái, số, gạch ngang và gạch dưới")
    private String sku;

    // [1E.x] Name: Bắt buộc, max 255
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm tối đa 255 ký tự")
    private String name;

    // Model code: Tùy chọn, max 100, an toàn ký tự
    @Size(max = 100, message = "Mã model tối đa 100 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9\\s-]*$", message = "Mã model chứa ký tự không hợp lệ")
    private String modelCode;

    // =========================================================================
    // 2. NHÓM PHÂN LOẠI
    // =========================================================================

    // Danh mục: Bắt buộc >= 1
    @NotEmpty(message = "2E.1: Phải chọn ít nhất một danh mục")
    private List<String> categoryIds;

    // Thương hiệu: Bắt buộc
    @NotBlank(message = "Vui lòng chọn thương hiệu")
    private String brandId;

    // Nhà cung cấp: Tùy chọn
    private String supplierId;

    // Xuất xứ: Tùy chọn, max 100, chặn ký tự đặc biệt nguy hiểm (<, >)
    @Size(max = 100, message = "Xuất xứ tối đa 100 ký tự")
    @Pattern(regexp = "^[^<>]*$", message = "Xuất xứ chứa ký tự không hợp lệ")
    private String origin;

    // Đơn vị tính: Bắt buộc (Cái, Bộ, Hộp...)
    @NotBlank(message = "Đơn vị tính không được để trống")
    private String unit;

    // =========================================================================
    // 3. NHÓM TÀI CHÍNH - GIÁ & THUẾ
    // =========================================================================

    // Giá nhập: >= 0, Tùy chọn
    @DecimalMin(value = "0.0", message = "Giá nhập phải lớn hơn hoặc bằng 0")
    private BigDecimal importPrice;

    // Giá niêm yết (Gạch ngang): >= 0, Tùy chọn
    @DecimalMin(value = "0.0", message = "Giá niêm yết phải lớn hơn hoặc bằng 0")
    private BigDecimal originalPrice;

    // Giá bán lẻ: Bắt buộc, >= 0
    @NotNull(message = "Giá bán lẻ không được để trống")
    @DecimalMin(value = "0.0", message = "Giá bán lẻ phải lớn hơn hoặc bằng 0")
    private BigDecimal salePrice;

    // Giá bán sỉ: >= 0, Tùy chọn
    @DecimalMin(value = "0.0", message = "Giá bán sỉ phải lớn hơn hoặc bằng 0")
    private BigDecimal wholesalePrice;

    // VAT: Tùy chọn (Service sẽ check cụ thể 0,5,8,10)
    @Min(value = 0, message = "VAT không được âm")
    private Integer vatRate = 0; // Mặc định 0

    // Tiền tệ: Bắt buộc
    @NotBlank(message = "Đơn vị tiền tệ không được để trống")
    private String currency = "VND"; // Mặc định VND

    // =========================================================================
    // 4. NHÓM KHO VẬN
    // =========================================================================

    // Tồn kho: >= 0
    @Min(value = 0, message = "Tồn kho không được âm")
    private Integer stockQuantity = 0; // Mặc định 0

    // Cảnh báo tồn kho thấp
    @Min(value = 0, message = "Mức cảnh báo tồn kho không được âm")
    private Integer minStockAlert = 0;

    // Trọng lượng (gram)
    @Min(value = 0, message = "Trọng lượng phải lớn hơn hoặc bằng 0")
    private Integer weightG;

    // Kích thước (cm)
    @Min(value = 0, message = "Chiều dài phải lớn hơn hoặc bằng 0")
    private Integer lengthCm;

    @Min(value = 0, message = "Chiều rộng phải lớn hơn hoặc bằng 0")
    private Integer widthCm;

    @Min(value = 0, message = "Chiều cao phải lớn hơn hoặc bằng 0")
    private Integer heightCm;

    // =========================================================================
    // 5. NHÓM NỘI DUNG & HÌNH ẢNH
    // =========================================================================

    @NotBlank(message = "Ảnh đại diện (thumbnail) không được để trống")
    private String thumbnail; // URL

    private List<String> gallery; // List URL

    private String videoUrl; // URL

    @Size(max = 500, message = "Mô tả ngắn tối đa 500 ký tự")
    private String shortDesc;

    // Full description (HTML) - Service sẽ lo việc sanitize XSS
    private String description;

    // Tags: Mỗi tag max 50 ký tự (Validation List phức tạp nên để Service lo hoặc dùng Custom Annotation)
    private List<String> tags;

    // =========================================================================
    // 6. NHÓM THỐNG KÊ (Thường dùng khi Import dữ liệu cũ)
    // =========================================================================

    @Min(value = 0)
    private Integer viewCount = 0;

    @Min(value = 0)
    private Integer soldCount = 0;

    @Min(value = 0)
    private Integer reviewCount = 0;

    @DecimalMin("0.0")
    @DecimalMax("5.0")
    private Float ratingAvg = 0f;

    // =========================================================================
    // 7. THỜI GIAN
    // =========================================================================

    // Ngày sản xuất: Không được lớn hơn hiện tại
    @PastOrPresent(message = "Ngày sản xuất không được ở tương lai")
    private LocalDate manufactureDate;

    // Hạn sử dụng: Tương lai
    @Future(message = "Hạn sử dụng phải ở tương lai") // Tùy business, có thể bỏ nếu cho phép bán hàng hết hạn
    private LocalDate expiryDate;
}