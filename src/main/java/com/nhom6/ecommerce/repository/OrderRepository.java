package com.nhom6.ecommerce.repository;

import com.nhom6.ecommerce.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Có thể thêm hàm tìm đơn theo User để xem lịch sử
}