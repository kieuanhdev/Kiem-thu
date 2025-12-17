package com.nhom6.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import java.util.List;

@Entity
@Table(name = "products")
@Data // Lombok sẽ tự sinh ra getter/setter cho TẤT CẢ các trường dưới đây
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Hoặc Identity tùy DB của bạn
    private String id;

    // --- 1. ĐỊNH DANH ---
    @Column(unique = true, nullable = false)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(name = "model_code")
    private String modelCode; // Mới thêm

    // --- 2. PHÂN LOẠI ---
    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToMany
    @JoinTable(
            name = "product_categories",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories;

    @Column(name = "supplier_id")
    private String supplierId; // Mới thêm

    private String origin;

    private String unit;

    // --- 3. GIÁ & THUẾ ---
    @Column(name = "import_price")
    private BigDecimal importPrice;

    @Column(name = "sale_price", nullable = false)
    private BigDecimal salePrice;

    @Column(name = "original_price")
    private BigDecimal originalPrice;

    @Column(name = "wholesale_price")
    private BigDecimal wholesalePrice; // Mới thêm

    @Column(name = "vat_rate")
    private Integer vatRate; // Mới thêm

    private String currency; // Mới thêm (VND, USD)

    // --- 4. KHO VẬN ---
    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;

    @Column(name = "min_stock_alert")
    private Integer minStockAlert = 0; // Mới thêm

    @Column(name = "weight_g")
    private Integer weightG; // Mới thêm

    @Column(name = "length_cm")
    private Integer lengthCm; // Mới thêm

    @Column(name = "width_cm")
    private Integer widthCm; // Mới thêm

    @Column(name = "height_cm")
    private Integer heightCm; // Mới thêm

    // --- 5. NỘI DUNG ---
    private String thumbnail;

    @ElementCollection // Lưu danh sách ảnh dưới dạng bảng phụ hoặc JSON tùy DB
    private List<String> gallery;

    @Column(name = "video_url")
    private String videoUrl; // Mới thêm

    @Column(name = "short_desc", length = 500)
    private String shortDesc; // Mới thêm

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    private List<String> tags; // Mới thêm (Từ khóa tìm kiếm)

    // --- 6. THỐNG KÊ (Hệ thống tự tính) ---
    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "sold_count")
    private Integer soldCount = 0;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(name = "rating_avg")
    private Float ratingAvg = 0f;

    // --- 7. THỜI GIAN ---
    @Column(name = "manufacture_date")
    @Temporal(TemporalType.DATE)
    private Date manufactureDate; // Mới thêm

    @Column(name = "expiry_date")
    @Temporal(TemporalType.DATE)
    private Date expiryDate; // Mới thêm

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();

    @Column(name = "is_active")
    private boolean isActive = true;
}