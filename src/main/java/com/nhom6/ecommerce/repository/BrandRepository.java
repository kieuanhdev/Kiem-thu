package com.nhom6.ecommerce.repository;

import com.nhom6.ecommerce.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    // Tìm kiếm thương hiệu theo tên (để tránh tạo trùng tên thương hiệu)
    Optional<Brand> findByName(String name);

    // Kiểm tra thương hiệu có tồn tại không theo ID
    boolean existsById(Long id);
}