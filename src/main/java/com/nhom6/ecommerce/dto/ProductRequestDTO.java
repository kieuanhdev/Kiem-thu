package com.nhom6.ecommerce.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data // Lombok sẽ tự tạo getter/setter: getBrandId(), getCategoryIds(),...
public class ProductRequestDTO {

    // --- 1. ĐỊNH DANH ---
    private String sku;
    private String name;
    private String modelCode;

    // --- 2. PHÂN LOẠI ---
    private String brandId;         // -> Sửa lỗi getBrandId()

    // Lưu ý: Nếu ID Category trong DB là số (Long), dùng List<Long>. Nếu là UUID, dùng List<String>
    private List<Long> categoryIds; // -> Sửa lỗi getCategoryIds()

    private String supplierId;
    private String origin;
    private String unit;

    // --- 3. GIÁ & THUẾ ---
    private BigDecimal importPrice;
    private BigDecimal salePrice;
    private BigDecimal originalPrice;
    private BigDecimal wholesalePrice;
    private Integer vatRate;
    private String currency;

    // --- 4. KHO VẬN ---
    private Integer stockQuantity;
    private Integer minStockAlert;
    private Integer weightG;
    private Integer lengthCm;
    private Integer widthCm;
    private Integer heightCm;

    // --- 5. NỘI DUNG ---
    private String thumbnail;
    private List<String> gallery;
    private String videoUrl;
    private String shortDesc;
    private String description;
    private List<String> tags;

    // --- 6. THỐNG KÊ (Optional - thường frontend không gửi cái này khi tạo mới) ---
    private Float ratingAvg;
    private Integer reviewCount;

    // --- 7. THỜI GIAN ---
    private Date manufactureDate;   // -> Sửa lỗi getManufactureDate()
    private Date expiryDate;        // -> Sửa lỗi getExpiryDate()
}