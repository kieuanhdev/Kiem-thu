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
    @Autowired private SupplierRepository supplierRepository; // Bổ sung repo

    // Danh sách đơn vị tính hợp lệ (nên cấu hình trong DB hoặc Config)
    private static final Set<String> ALLOWED_UNITS = Set.of("Cái", "Bộ", "Hộp", "Kg", "Thùng");
    private static final Set<String> ALLOWED_CURRENCIES = Set.of("VND", "USD");

    @Transactional
    public Product createProduct(ProductRequestDTO req) {
        List<String> warnings = new ArrayList<>(); // Danh sách chứa cảnh báo (3W, 4W, 5W...)

        // --- 1. NHÓM ĐỊNH DANH ---
        // Validate SKU duy nhất
        if (productRepository.existsBySku(req.getSku())) {
            throw new RuntimeException("1E.7: SKU đã tồn tại trong hệ thống");
        }

        // Validate Model Code trùng trong cùng Brand
        if (req.getModelCode() != null && !req.getModelCode().isEmpty()) {
            if (productRepository.existsByBrandIdAndModelCode(req.getBrandId(), req.getModelCode())) {
                throw new RuntimeException("Model code đã tồn tại cho thương hiệu này.");
            }
        }

        // --- 2. NHÓM PHÂN LOẠI ---
        Brand brand = brandRepository.findById(req.getBrandId())
                .orElseThrow(() -> new RuntimeException("Thương hiệu không tồn tại"));

        List<Category> categories = categoryRepository.findAllById(req.getCategoryIds());
        if (categories.isEmpty()) {
            throw new RuntimeException("2E.1: Phải chọn ít nhất một danh mục");
        }
        // Warning 5W.2: Nhiều danh mục nhưng không có tags
        if (categories.size() >= 2 && (req.getTags() == null || req.getTags().isEmpty())) {
            warnings.add("5W.2: Sản phẩm thuộc nhiều danh mục, nên thêm từ khóa tìm kiếm (tags).");
        }

        // Validate Supplier (Nếu có nhập)
        if (req.getSupplierId() != null && !supplierRepository.existsById(req.getSupplierId())) {
            throw new RuntimeException("Nhà cung cấp không tồn tại.");
        }

        // Validate Unit
        if (!ALLOWED_UNITS.contains(req.getUnit())) {
            throw new RuntimeException("Đơn vị tính không hợp lệ.");
        }

        // --- 3. NHÓM GIÁ & THUẾ ---
        BigDecimal importPrice = req.getImportPrice();
        BigDecimal salePrice = req.getSalePrice();
        BigDecimal originalPrice = req.getOriginalPrice();
        BigDecimal wholesalePrice = req.getWholesalePrice();

        // 3E.12: Giá bán lẻ < Giá nhập
        if (importPrice != null && salePrice.compareTo(importPrice) < 0) {
            throw new RuntimeException("3E.12: Giá bán lẻ không được nhỏ hơn giá nhập");
        }

        // 3E.5: Giá niêm yết < Giá bán lẻ
        if (originalPrice != null && originalPrice.compareTo(salePrice) < 0) {
            throw new RuntimeException("3E.5: Giá niêm yết không được nhỏ hơn giá bán lẻ");
        }

        // 3E.8: Giá sỉ > Giá lẻ
        if (wholesalePrice != null && wholesalePrice.compareTo(salePrice) > 0) {
            throw new RuntimeException("3E.8: Giá bán sỉ không được lớn hơn giá bán lẻ");
        }

        // 3E.13: VAT & Currency
        if (!ALLOWED_CURRENCIES.contains(req.getCurrency())) {
            throw new RuntimeException("Đơn vị tiền tệ không được hỗ trợ.");
        }
        if ("VND".equals(req.getCurrency())) {
            int vat = req.getVatRate() != null ? req.getVatRate() : 0;
            if (!Set.of(0, 5, 8, 10).contains(vat)) {
                throw new RuntimeException("3E.13: Thuế suất VAT không phù hợp với VND (0, 5, 8, 10).");
            }
        }

        // --- 4. NHÓM KHO VẬN ---
        Integer len = req.getLengthCm();
        Integer wid = req.getWidthCm();
        Integer hei = req.getHeightCm();
        Integer wei = req.getWeightG();

        // 4E.7: Nhập thiếu kích thước
        boolean hasDim = (len != null || wid != null || hei != null);
        boolean fullDim = (len != null && wid != null && hei != null);
        if (hasDim && !fullDim) {
            throw new RuntimeException("4E.7: Vui lòng nhập đầy đủ chiều dài, rộng, cao của gói hàng");
        }

        // 4E.8: Có kích thước nhưng thiếu cân nặng
        if (fullDim && (wei == null || wei <= 0)) {
            throw new RuntimeException("4E.8: Vui lòng nhập trọng lượng sản phẩm để tính phí vận chuyển");
        }

        // 4W.1: Tồn kho dưới mức cảnh báo
        int stock = req.getStockQuantity() != null ? req.getStockQuantity() : 0;
        int minAlert = req.getMinStockAlert() != null ? req.getMinStockAlert() : 0;
        if (minAlert > 0 && stock < minAlert) {
            warnings.add("4W.1: Sản phẩm đang dưới mức tồn kho cảnh báo.");
        }

        // --- 5. NHÓM NỘI DUNG ---
        // 5W.1: Có video nhưng thiếu ảnh Gallery
        if (req.getVideoUrl() != null && !req.getVideoUrl().isEmpty()) {
            if (req.getGallery() == null || req.getGallery().isEmpty()) {
                warnings.add("5W.1: Nên bổ sung ít nhất một ảnh minh họa khi có video review.");
            }
        }

        // --- 6. NHÓM THỐNG KÊ ---
        // 5E.7 & 5E.8: Rating logic
        Float rating = req.getRatingAvg() != null ? req.getRatingAvg() : 0f;
        Integer reviews = req.getReviewCount() != null ? req.getReviewCount() : 0;

        if (rating > 0 && reviews <= 0) {
            throw new RuntimeException("5E.7: Số lượng đánh giá phải lớn hơn 0 khi có điểm đánh giá");
        }
        if (reviews == 0 && rating > 0) {
            throw new RuntimeException("5E.8: Không thể có điểm đánh giá khi chưa có đánh giá nào");
        }

        // --- 7. NHÓM THỜI GIAN ---
        // 6E.4: Ngày SX > Hạn SD
        if (req.getManufactureDate() != null && req.getExpiryDate() != null) {
            if (req.getManufactureDate().after(req.getExpiryDate())) {
                throw new RuntimeException("6E.4: Hạn sử dụng không được nhỏ hơn ngày sản xuất");
            }
        }

        // --- MAPPING & SAVE ---
        Product product = new Product();
        product.setSku(req.getSku());
        product.setName(req.getName());
        product.setModelCode(req.getModelCode()); // Mới thêm
        product.setBrand(brand);
        product.setCategories(new HashSet<>(categories));
        product.setSupplierId(req.getSupplierId()); // Mới thêm
        product.setOrigin(req.getOrigin());
        product.setUnit(req.getUnit());

        // Giá
        product.setImportPrice(importPrice);
        product.setSalePrice(salePrice);
        product.setOriginalPrice(originalPrice);
        product.setWholesalePrice(wholesalePrice);
        product.setVatRate(req.getVatRate() != null ? req.getVatRate() : 0);
        product.setCurrency(req.getCurrency());

        // Kho
        product.setStockQuantity(stock);
        product.setMinStockAlert(minAlert);
        product.setWeightG(wei);
        product.setLengthCm(len);
        product.setWidthCm(wid);
        product.setHeightCm(hei);

        // Nội dung
        product.setThumbnail(req.getThumbnail());
        product.setGallery(req.getGallery());
        product.setVideoUrl(req.getVideoUrl());
        product.setShortDesc(req.getShortDesc());
        product.setDescription(req.getDescription()); // full_desc
        product.setTags(req.getTags());

        // Thời gian
        product.setManufactureDate(req.getManufactureDate());
        product.setExpiryDate(req.getExpiryDate());

        // Xử lý warnings (Tùy business: có thể log ra console hoặc lưu vào field transient để trả về Controller)
        if (!warnings.isEmpty()) {
            System.out.println("WARNINGS KHI TẠO SP SKU " + req.getSku() + ": " + warnings);
            // product.setWarnings(warnings); // Nếu Entity có field @Transient warnings
        }

        return productRepository.save(product);
    }
}