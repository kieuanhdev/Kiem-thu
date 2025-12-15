package com.nhom6.ecommerce.service;

import com.nhom6.ecommerce.dto.ReturnRequestDTO;
import com.nhom6.ecommerce.entity.*;
import com.nhom6.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class ReturnService {

    @Autowired private ReturnRequestRepository returnRequestRepository; // (Tạo interface này tương tự các repo khác)
    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;

    public ReturnRequest createReturnRequest(ReturnRequestDTO req) {
        // 1. Tìm đơn hàng
        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new RuntimeException("404: Không tìm thấy đơn hàng"));

        // 2. Validate Trạng thái đơn hàng [cite: 390-391]
        if (order.getStatus() != Order.OrderStatus.COMPLETED && order.getStatus() != Order.OrderStatus.DELIVERED) {
            throw new RuntimeException("9E.1: Đơn hàng chưa được giao, không thể yêu cầu trả hàng");
        }

        // 3. Validate Thời gian (15 ngày đổi trả) [cite: 393-394]
        // Lưu ý: Nếu deliveryDate null (do dữ liệu cũ), ta tạm dùng createdAt để test
        LocalDateTime milestoneDate = order.getDeliveryDate() != null ? order.getDeliveryDate() : order.getCreatedAt();
        long daysBetween = ChronoUnit.DAYS.between(milestoneDate, LocalDateTime.now());

        if (daysBetween > 15) {
            throw new RuntimeException("9E.2: Đã quá thời hạn đổi trả (15 ngày)");
        }

        // 4. Validate Sản phẩm trong đơn (Có mua mới được trả) [cite: 333]
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        boolean productInOrder = order.getItems().stream()
                .anyMatch(item -> item.getProduct().getId().equals(req.getProductId()));

        if (!productInOrder) {
            throw new RuntimeException("9E.3: Sản phẩm không thuộc đơn hàng này");
        }

        // 5. Validate Bank Info (Nếu chọn Chuyển khoản) [cite: 417-425]
        if (req.getRefundMethod() == ReturnRequest.RefundMethod.BANK_TRANSFER) {
            if (req.getBankName() == null || req.getBankAccountNumber() == null) {
                throw new RuntimeException("9E.9: Vui lòng nhập đầy đủ thông tin ngân hàng");
            }
        }

        // 6. Lưu yêu cầu
        ReturnRequest returnReq = new ReturnRequest();
        returnReq.setOrder(order);
        returnReq.setUser(order.getUser());
        returnReq.setProduct(product);
        returnReq.setQuantity(req.getQuantity());
        returnReq.setReason(req.getReason());
        returnReq.setDescription(req.getDescription());
        returnReq.setProofImages(req.getProofImages());
        returnReq.setRefundMethod(req.getRefundMethod());

        if (req.getRefundMethod() == ReturnRequest.RefundMethod.BANK_TRANSFER) {
            returnReq.setBankName(req.getBankName());
            returnReq.setBankAccountNumber(req.getBankAccountNumber());
            returnReq.setBankAccountName(req.getBankAccountName());
        }

        // Cập nhật trạng thái đơn hàng (Optional, tùy quy trình)
        // order.setStatus(Order.OrderStatus.RETURN_REQUESTED);
        // orderRepository.save(order);

        return returnRequestRepository.save(returnReq);
    }
}