package com.nhom6.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    // 1. Thông tin định danh [cite: 823-847]
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id; // Hệ thống tự sinh UUID

    @Column(nullable = false, unique = true, length = 50)
    private String sku; // Mã SKU duy nhất

    @Column(nullable = false)
    private String name; // Tên sản phẩm

    @Column(name = "model_code", length = 100)
    private String modelCode;

    // 2. Phân loại & Quan hệ [cite: 850-877]
    @ManyToOne
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    // Một sản phẩm thuộc nhiều danh mục
    @ManyToMany
    @JoinTable(
            name = "product_categories",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories;

    private String origin; // Xuất xứ
    private String unit;   // Đơn vị tính

    // 3. Thông tin tài chính [cite: 881-933]
    @Column(name = "import_price")
    private BigDecimal importPrice;

    @Column(name = "sale_price", nullable = false)
    private BigDecimal salePrice; // Giá bán lẻ

    @Column(name = "original_price")
    private BigDecimal originalPrice; // Giá niêm yết

    // 4. Kho vận [cite: 938-963]
    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;

    private Integer weightG; // Trọng lượng (gram)
    private Integer lengthCm;
    private Integer widthCm;
    private Integer heightCm;

    // 5. Nội dung & Media [cite: 974-1016]
    @Column(nullable = false)
    private String thumbnail; // Ảnh đại diện

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> gallery; // Album ảnh

    @Column(columnDefinition = "TEXT")
    private String description; // HTML mô tả chi tiết

    @ElementCollection
    @CollectionTable(name = "product_tags", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "tag")
    private List<String> tags; // Từ khóa tìm kiếm

    // 6. Thời gian & Hạn sử dụng [cite: 1034-1053]
    private LocalDate manufactureDate;
    private LocalDate expiryDate;

    // Meta data
    private boolean isActive = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}