package com.nhom6.ecommerce.repository;

import com.nhom6.ecommerce.entity.ReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    // Tìm các yêu cầu trả hàng của một user (để xem lịch sử)
    List<ReturnRequest> findByUserId(String userId);

    // Tìm yêu cầu trả hàng theo mã đơn hàng
    List<ReturnRequest> findByOrderId(Long orderId);
}