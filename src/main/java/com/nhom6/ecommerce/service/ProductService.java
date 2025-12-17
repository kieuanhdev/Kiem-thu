package com.nhom6.ecommerce.service;

import com.nhom6.ecommerce.dto.ProductRequestDTO;
import com.nhom6.ecommerce.entity.*;
import com.nhom6.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProductService {

    @Autowired private ProductRepository productRepository;
    @Autowired private BrandRepository brandRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private SupplierRepository supplierRepository;

    // Cấu hình (Nên chuyển sang file config hoặc DB nếu hệ thống lớn)
    private static final Set<String> ALLOWED_UNITS = Set.of("Cái", "Bộ", "Hộp", "Kg", "Thùng");
    private static final Set<String> ALLOWED_CURRENCIES = Set.of("VND", "USD");

    /**
     * HÀM CHÍNH: TẠO SẢN PHẨM
     * Luồng xử lý tuyến tính, rõ ràng.
     */
    @Transactional
    public Product createProduct(ProductRequestDTO req) {
        List<String> warnings = new ArrayList<>();

        // 1. Validate Định danh (SKU, Model)
        validateIdentity(req);

        // 2. Validate & Lấy dữ liệu Phân loại (Brand, Category, Supplier, Unit)
        Brand brand = getAndValidateBrand(req.getBrandId());
        List<Category> categories = getAndValidateCategories(req, warnings);
        validateSupplier(req.getSupplierId());
        validateUnit(req.getUnit());

        // 3. Validate Giá & Thuế
        validatePricingAndTax(req);

        // 4. Validate Kho vận (Kích thước, Cảnh báo tồn kho)
        validateInventory(req, warnings);

        // 5. Validate Nội dung & Thống kê
        validateContent(req, warnings);
        validateStatistics(req);

        // 6. Validate Thời gian
        validateTime(req);

        // 7. Mapping & Lưu xuống DB
        return mapAndSaveProduct(req, brand, categories, warnings);
    }

    // =========================================================================
    // CÁC HÀM VALIDATION NHỎ (PRIVATE METHODS)
    // =========================================================================

    /**
     * 1. Validate Nhóm Định danh
     */
    private void validateIdentity(ProductRequestDTO req) {
        // [1E.7] Check SKU
        if (productRepository.existsBySku(req.getSku())) {
            throw new RuntimeException("1E.7: SKU đã tồn tại trong hệ thống");
        }
        // Check Model Code
        if (req.getModelCode() != null && !req.getModelCode().isEmpty()) {
            if (productRepository.existsByBrandIdAndModelCode(req.getBrandId(), req.getModelCode())) {
                throw new RuntimeException("Model code đã tồn tại cho thương hiệu này.");
            }
        }
    }

    /**
     * 2.1 Lấy & Validate Brand
     */
    private Brand getAndValidateBrand(String brandId) {
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new RuntimeException("Thương hiệu không tồn tại"));
    }

    /**
     * 2.2 Lấy & Validate Categories (kèm cảnh báo Tags)
     */
    private List<Category> getAndValidateCategories(ProductRequestDTO req, List<String> warnings) {
        List<Category> categories = categoryRepository.findAllById(req.getCategoryIds());
        if (categories.isEmpty()) {
            throw new RuntimeException("2E.1: Phải chọn ít nhất một danh mục");
        }
        // [5W.2] Warning Tags
        if (categories.size() >= 2 && (req.getTags() == null || req.getTags().isEmpty())) {
            warnings.add("5W.2: Sản phẩm thuộc nhiều danh mục, nên thêm từ khóa tìm kiếm (tags).");
        }
        return categories;
    }

    /**
     * 2.3 Validate Nhà cung cấp
     */
    private void validateSupplier(String supplierId) {
        if (supplierId != null && !supplierRepository.existsById(supplierId)) {
            throw new RuntimeException("Nhà cung cấp không tồn tại.");
        }
    }

    /**
     * 2.4 Validate Đơn vị tính
     */
    private void validateUnit(String unit) {
        if (!ALLOWED_UNITS.contains(unit)) {
            throw new RuntimeException("Đơn vị tính không hợp lệ.");
        }
    }

    /**
     * 3. Validate Giá & Thuế
     */
    private void validatePricingAndTax(ProductRequestDTO req) {
        BigDecimal importPrice = req.getImportPrice();
        BigDecimal salePrice = req.getSalePrice();
        BigDecimal originalPrice = req.getOriginalPrice();
        BigDecimal wholesalePrice = req.getWholesalePrice();

        // [3E.12]
        if (importPrice != null && salePrice.compareTo(importPrice) < 0) {
            throw new RuntimeException("3E.12: Giá bán lẻ không được nhỏ hơn giá nhập");
        }
        // [3E.5]
        if (originalPrice != null && originalPrice.compareTo(salePrice) < 0) {
            throw new RuntimeException("3E.5: Giá niêm yết không được nhỏ hơn giá bán lẻ");
        }
        // [3E.8]
        if (wholesalePrice != null && wholesalePrice.compareTo(salePrice) > 0) {
            throw new RuntimeException("3E.8: Giá bán sỉ không được lớn hơn giá bán lẻ");
        }
        // [3E.13] Currency & VAT
        if (!ALLOWED_CURRENCIES.contains(req.getCurrency())) {
            throw new RuntimeException("Đơn vị tiền tệ không được hỗ trợ.");
        }
        if ("VND".equals(req.getCurrency())) {
            int vat = req.getVatRate() != null ? req.getVatRate() : 0;
            if (!Set.of(0, 5, 8, 10).contains(vat)) {
                throw new RuntimeException("3E.13: Thuế suất VAT không phù hợp với VND (0, 5, 8, 10).");
            }
        }
    }

    /**
     * 4. Validate Kho vận
     */
    private void validateInventory(ProductRequestDTO req, List<String> warnings) {
        Integer len = req.getLengthCm();
        Integer wid = req.getWidthCm();
        Integer hei = req.getHeightCm();
        Integer wei = req.getWeightG();

        boolean hasDim = (len != null || wid != null || hei != null);
        boolean fullDim = (len != null && wid != null && hei != null);

        // [4E.7]
        if (hasDim && !fullDim) {
            throw new RuntimeException("4E.7: Vui lòng nhập đầy đủ chiều dài, rộng, cao của gói hàng");
        }
        // [4E.8]
        if (fullDim && (wei == null || wei <= 0)) {
            throw new RuntimeException("4E.8: Vui lòng nhập trọng lượng sản phẩm để tính phí vận chuyển");
        }

        // [4W.1] Cảnh báo tồn kho
        int stock = req.getStockQuantity() != null ? req.getStockQuantity() : 0;
        int minAlert = req.getMinStockAlert() != null ? req.getMinStockAlert() : 0;
        if (minAlert > 0 && stock < minAlert) {
            warnings.add("4W.1: Sản phẩm đang dưới mức tồn kho cảnh báo.");
        }
    }

    /**
     * 5. Validate Nội dung (Video/Gallery)
     */
    private void validateContent(ProductRequestDTO req, List<String> warnings) {
        // [5W.1]
        if (req.getVideoUrl() != null && !req.getVideoUrl().isEmpty()) {
            if (req.getGallery() == null || req.getGallery().isEmpty()) {
                warnings.add("5W.1: Nên bổ sung ít nhất một ảnh minh họa khi có video review.");
            }
        }
    }

    /**
     * 6. Validate Thống kê (Rating/Review)
     */
    private void validateStatistics(ProductRequestDTO req) {
        Float rating = req.getRatingAvg() != null ? req.getRatingAvg() : 0f;
        Integer reviews = req.getReviewCount() != null ? req.getReviewCount() : 0;

        // [5E.7]
        if (rating > 0 && reviews <= 0) {
            throw new RuntimeException("5E.7: Số lượng đánh giá phải lớn hơn 0 khi có điểm đánh giá");
        }
        // [5E.8]
        if (reviews == 0 && rating > 0) {
            throw new RuntimeException("5E.8: Không thể có điểm đánh giá khi chưa có đánh giá nào");
        }
    }

    /**
     * 7. Validate Thời gian
     */
    private void validateTime(ProductRequestDTO req) {
        // [6E.4]
        if (req.getManufactureDate() != null && req.getExpiryDate() != null) {
            if (req.getManufactureDate().after(req.getExpiryDate())) {
                throw new RuntimeException("6E.4: Hạn sử dụng không được nhỏ hơn ngày sản xuất");
            }
        }
    }

    /**
     * 8. Mapping và Lưu
     */
    private Product mapAndSaveProduct(ProductRequestDTO req, Brand brand, List<Category> categories, List<String> warnings) {
        Product product = new Product();

        // Identity & Class
        product.setSku(req.getSku());
        product.setName(req.getName());
        product.setModelCode(req.getModelCode());
        product.setBrand(brand);
        product.setCategories(new HashSet<>(categories));
        product.setSupplierId(req.getSupplierId());
        product.setOrigin(req.getOrigin());
        product.setUnit(req.getUnit());

        // Price & Tax
        product.setImportPrice(req.getImportPrice());
        product.setSalePrice(req.getSalePrice());
        product.setOriginalPrice(req.getOriginalPrice());
        product.setWholesalePrice(req.getWholesalePrice());
        product.setVatRate(req.getVatRate() != null ? req.getVatRate() : 0);
        product.setCurrency(req.getCurrency());

        // Inventory
        product.setStockQuantity(req.getStockQuantity() != null ? req.getStockQuantity() : 0);
        product.setMinStockAlert(req.getMinStockAlert());
        product.setWeightG(req.getWeightG());
        product.setLengthCm(req.getLengthCm());
        product.setWidthCm(req.getWidthCm());
        product.setHeightCm(req.getHeightCm());

        // Content
        product.setThumbnail(req.getThumbnail());
        product.setGallery(req.getGallery());
        product.setVideoUrl(req.getVideoUrl());
        product.setShortDesc(req.getShortDesc());
        product.setDescription(req.getDescription());
        product.setTags(req.getTags());

        // Time
        product.setManufactureDate(req.getManufactureDate());
        product.setExpiryDate(req.getExpiryDate());

        // Handle Warnings (Log or transient field)
        if (!warnings.isEmpty()) {
            System.out.println("WARNINGS [" + req.getSku() + "]: " + warnings);
            // product.setWarnings(warnings); // Uncomment nếu Entity có field này
        }

        return productRepository.save(product);
    }
}