package com.nhom6.ecommerce.repository;

import com.nhom6.ecommerce.entity.Order;
import com.nhom6.ecommerce.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByOrderIdAndProductId(Long orderId, String productId);
}