package com.nhom6.ecommerce.repository;

import com.nhom6.ecommerce.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Kiểm tra xem User có đơn hàng nào chứa Product đó và trạng thái là COMPLETED không
    // [cite: 657-661] - Yêu cầu logic: Đã mua & Đã hoàn tất
    @Query("SELECT o FROM Order o JOIN o.items i " +
            "WHERE o.user.userId = :userId " +
            "AND i.product.id = :productId " +
            "AND o.status = 'COMPLETED'")
    Optional<Order> findCompletedOrderWithProduct(@Param("userId") String userId,
                                                  @Param("productId") String productId);
}