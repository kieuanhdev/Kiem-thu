package com.nhom6.ecommerce.repository;

import com.nhom6.ecommerce.entity.ReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    // SỬA DÒNG NÀY:
    // Cũ (Lỗi): List<ReturnRequest> findByUserId(String userId);
    // Mới (Đúng): Thêm "User_" hoặc "UserUserId" để map đúng vào user.userId
    List<ReturnRequest> findByUser_UserId(String userId);

    // Hàm này giữ nguyên vì trong ReturnRequest có trường order (và Order có id là Long id)
    List<ReturnRequest> findByOrderId(Long orderId);

    @Query("SELECT COALESCE(SUM(r.quantity), 0) FROM ReturnRequest r " +
            "WHERE r.order.id = :orderId " +  // <-- SỬA: dùng .id (không phải .orderId)
            "AND r.product.id = :productId " + // Giữ nguyên nếu Product dùng String ID
            "AND r.status != 'REJECTED'")
    int sumQuantityByOrderIdAndProductId(@Param("orderId") Long orderId, // <-- SỬA: Kiểu Long
                                         @Param("productId") String productId);
}