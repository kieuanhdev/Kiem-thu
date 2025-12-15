package com.nhom6.ecommerce.repository;

import com.nhom6.ecommerce.entity.ReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;
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
}