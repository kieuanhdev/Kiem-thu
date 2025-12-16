package com.nhom6.ecommerce.repository;
import com.nhom6.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, String> {
    boolean existsBySku(String sku); // Kiểm tra trùng SKU [cite: 834]
    boolean existsByBrandIdAndModelCode(String brandId, String modelCode);
}