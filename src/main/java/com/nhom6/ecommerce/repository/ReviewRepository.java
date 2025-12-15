package com.nhom6.ecommerce.repository;

import com.nhom6.ecommerce.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Lấy danh sách review của một sản phẩm
    List<Review> findByProductId(String productId);

    // Kiểm tra xem user đã đánh giá sản phẩm này trong đơn hàng này chưa (tránh spam)
    boolean existsByOrderIdAndProductId(Long orderId, String productId);
}