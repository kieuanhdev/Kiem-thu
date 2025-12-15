package com.nhom6.ecommerce.service;

import com.nhom6.ecommerce.dto.ProductRequestDTO;
import com.nhom6.ecommerce.entity.*;
import com.nhom6.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.List;

@Service
public class ProductService {

    @Autowired private ProductRepository productRepository;
    @Autowired private BrandRepository brandRepository;
    @Autowired private CategoryRepository categoryRepository;

    public Product createProduct(ProductRequestDTO req) {
        // 1. Validate SKU duy nhất [cite: 834]
        if (productRepository.existsBySku(req.getSku())) {
            throw new RuntimeException("1E.7: SKU đã tồn tại trong hệ thống");
        }

        // 2. Validate Giá (Logic: Giá niêm yết >= Giá bán) [cite: 925-926]
        if (req.getOriginalPrice() != null && req.getOriginalPrice().compareTo(req.getSalePrice()) < 0) {
            throw new RuntimeException("3E.5: Giá niêm yết không được nhỏ hơn giá bán lẻ");
        }

        // 3. Lấy Brand & Categories từ DB
        Brand brand = brandRepository.findById(req.getBrandId())
                .orElseThrow(() -> new RuntimeException("Thương hiệu không tồn tại"));

        List<Category> categories = categoryRepository.findAllById(req.getCategoryIds());
        if (categories.isEmpty()) {
            throw new RuntimeException("2E.1: Phải chọn ít nhất một danh mục");
        }

        // 4. Map DTO sang Entity
        Product product = new Product();
        product.setSku(req.getSku());
        product.setName(req.getName());
        product.setBrand(brand);
        product.setCategories(new HashSet<>(categories));
        product.setSalePrice(req.getSalePrice());
        product.setOriginalPrice(req.getOriginalPrice());
        product.setImportPrice(req.getImportPrice());
        product.setStockQuantity(req.getStockQuantity() != null ? req.getStockQuantity() : 0);
        product.setThumbnail(req.getThumbnail());
        product.setGallery(req.getGallery());
        product.setDescription(req.getDescription());
        product.setTags(req.getTags());

        return productRepository.save(product);
    }
}