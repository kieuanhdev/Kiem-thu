package com.nhom6.ecommerce.repository;

import com.nhom6.ecommerce.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Tìm danh sách danh mục theo danh sách ID (dùng cho việc gắn nhiều danh mục vào sản phẩm)
    // JpaRepository đã có sẵn findAllById, nhưng đây là ví dụ nếu cần lọc thêm trạng thái Active
    List<Category> findByIdInAndIsActiveTrue(List<Long> ids);

    // Kiểm tra mã code danh mục có trùng không
    boolean existsByCode(String code);
}