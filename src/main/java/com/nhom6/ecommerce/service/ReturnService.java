package com.nhom6.ecommerce.service;

import com.nhom6.ecommerce.dto.ReturnRequestDTO;
import com.nhom6.ecommerce.entity.*;
import com.nhom6.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ReturnService {

    @Autowired private ReturnRequestRepository returnRequestRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;

    // Các hằng số Regex để validate thông tin ngân hàng
    private static final String BANK_NUMBER_REGEX = "^[0-9]{6,20}$";
    private static final String BANK_HOLDER_REGEX = "^[A-Z\\s]{3,50}$"; // Chữ in hoa không dấu

    /**
     * HÀM CHÍNH: TẠO YÊU CẦU TRẢ HÀNG
     */
    @Transactional
    public ReturnRequest createReturnRequest(ReturnRequestDTO req) {
        // 1. Validate Ngữ cảnh đơn hàng (Trạng thái, Thời gian)
        // Lưu ý: DTO phải gửi orderId là kiểu Long (hoặc ép kiểu từ String)
        Order order = validateOrderContext(req.getOrderId());

        // 2. Validate Sản phẩm & Số lượng khả dụng
        Product product = validateProductAndQuantity(order, req.getProductId(), req.getQuantity());

        // 3. Validate Lý do & Minh chứng (Ảnh/Video)
        validateReasonAndProofs(req);

        // 4. Validate Phương thức hoàn tiền
        validateRefundMethod(order, req.getRefundMethod());

        // 5. Validate Thông tin ngân hàng (Chỉ khi chọn chuyển khoản)
        if (req.getRefundMethod() == ReturnRequest.RefundMethod.BANK_TRANSFER) {
            validateBankInfo(req);
        }

        // 6. Lưu dữ liệu
        return saveReturnRequest(req, order, product);
    }

    // =========================================================================
    // CÁC HÀM VALIDATION NHỎ (PRIVATE METHODS)
    // =========================================================================

    /**
     * 1. Validate Ngữ cảnh: Kiểm tra sự tồn tại, trạng thái và hạn 15 ngày
     */
    private Order validateOrderContext(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("404: Không tìm thấy đơn hàng"));

        // [9E.1] Check trạng thái
        if (order.getStatus() != Order.OrderStatus.COMPLETED && order.getStatus() != Order.OrderStatus.DELIVERED) {
            throw new RuntimeException("9E.1: Đơn hàng chưa được giao, không thể yêu cầu trả hàng.");
        }

        // [9E.2] Check thời hạn 15 ngày
        LocalDateTime milestoneDate = order.getDeliveryDate() != null ? order.getDeliveryDate() : order.getCreatedAt();
        long daysBetween = ChronoUnit.DAYS.between(milestoneDate, LocalDateTime.now());

        if (daysBetween > 15) {
            throw new RuntimeException("9E.2: Đã quá thời hạn đổi trả (15 ngày).");
        }

        return order;
    }

    /**
     * 2. Validate Sản phẩm: Có trong đơn không, Số lượng trả có hợp lệ không
     */
    private Product validateProductAndQuantity(Order order, String productId, int quantityRequested) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        // Check sản phẩm có trong đơn hàng này không
        OrderItem orderItem = order.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("9E.3: Sản phẩm không thuộc đơn hàng này."));

        // [9E.4] Check Số lượng khả dụng
        // Gọi Repository với ID là Long (order.getId())
        int returnedQty = returnRequestRepository.sumQuantityByOrderIdAndProductId(order.getId(), productId);
        int availableQty = orderItem.getQuantity() - returnedQty;

        if (quantityRequested < 1 || quantityRequested > availableQty) {
            throw new RuntimeException("9E.4: Số lượng trả không hợp lệ. Khả dụng: " + availableQty);
        }

        return product;
    }

    /**
     * 3. Validate Lý do & Minh chứng: Check mô tả, ảnh, video theo từng lý do
     */
    private void validateReasonAndProofs(ReturnRequestDTO req) {
        ReturnRequest.ReturnReason reason = req.getReason();
        String desc = req.getDescription();
        List<String> images = req.getProofImages();
        List<String> videos = req.getProofVideos();

        // [9E.5] Check Mô tả chi tiết
        if (reason == ReturnRequest.ReturnReason.NOT_SATISFIED || reason == ReturnRequest.ReturnReason.OTHER) {
            if (desc == null || desc.trim().length() < 20) {
                throw new RuntimeException("9E.5: Vui lòng nhập chi tiết lý do (tối thiểu 20 ký tự).");
            }
        }

        // [9E.6] Check Video (Thiếu hàng)
        if (reason == ReturnRequest.ReturnReason.MISSING_ITEM) {
            if (videos == null || videos.isEmpty()) {
                throw new RuntimeException("9E.6: Vui lòng tải lên Video mở hộp cho lý do Thiếu hàng.");
            }
        }

        // [9E.7] Check Ảnh (Hư hỏng / Sai hàng / Không ưng ý cũng nên có ảnh)
        if (reason == ReturnRequest.ReturnReason.DAMAGED || reason == ReturnRequest.ReturnReason.WRONG_ITEM) {
            if (images == null || images.isEmpty()) {
                throw new RuntimeException("9E.7: Vui lòng tải lên hình ảnh sản phẩm bị lỗi.");
            }
        }

        // [9E.8] Check giới hạn file
        int totalFiles = (images != null ? images.size() : 0) + (videos != null ? videos.size() : 0);
        if (totalFiles > 5) {
            throw new RuntimeException("9E.8: Tối đa 5 tệp tin minh chứng.");
        }
    }

    /**
     * 4. Validate Phương thức hoàn tiền: Logic COD vs Online
     */
    private void validateRefundMethod(Order order, ReturnRequest.RefundMethod refundMethod) {
        if (order.getPaymentMethod() == Order.PaymentMethod.COD) {
            // Đơn COD không thể hoàn về nguồn (vì nguồn là tiền mặt)
            if (refundMethod == ReturnRequest.RefundMethod.ORIGINAL_METHOD) {
                throw new RuntimeException("Đơn hàng COD không thể hoàn tiền về nguồn thanh toán gốc.");
            }
        } else {
            // Đơn Online (VNPAY, Banking...) -> Bắt buộc hoàn về nguồn
            if (refundMethod == ReturnRequest.RefundMethod.BANK_TRANSFER) {
                throw new RuntimeException("Đơn hàng thanh toán Online sẽ được hoàn tiền về nguồn ban đầu.");
            }
        }
    }

    /**
     * 5. Validate Thông tin ngân hàng: Check null và Regex
     */
    private void validateBankInfo(ReturnRequestDTO req) {
        // [9E.9] Check Null
        if (req.getBankName() == null || req.getBankAccountNumber() == null || req.getBankAccountName() == null) {
            throw new RuntimeException("9E.9: Vui lòng nhập đầy đủ thông tin ngân hàng.");
        }

        // [9E.10] Check Số tài khoản
        if (!Pattern.matches(BANK_NUMBER_REGEX, req.getBankAccountNumber().trim())) {
            throw new RuntimeException("9E.10: Số tài khoản ngân hàng không hợp lệ (6-20 số).");
        }

        // [9E.11] Check Tên chủ tài khoản
        if (!Pattern.matches(BANK_HOLDER_REGEX, req.getBankAccountName().trim())) {
            throw new RuntimeException("9E.11: Tên chủ tài khoản không hợp lệ (In hoa không dấu, 3-50 ký tự).");
        }
    }

    /**
     * 6. Hàm hỗ trợ lưu dữ liệu
     */
    private ReturnRequest saveReturnRequest(ReturnRequestDTO req, Order order, Product product) {
        ReturnRequest returnReq = new ReturnRequest();
        returnReq.setOrder(order);
        returnReq.setUser(order.getUser());
        returnReq.setProduct(product);
        returnReq.setQuantity(req.getQuantity());
        returnReq.setReason(req.getReason());
        returnReq.setDescription(req.getDescription());
        returnReq.setProofImages(req.getProofImages());
        returnReq.setProofVideos(req.getProofVideos());
        returnReq.setRefundMethod(req.getRefundMethod());
        returnReq.setStatus(ReturnRequest.ReturnStatus.PENDING);
        returnReq.setCreatedAt(LocalDateTime.now());

        if (req.getRefundMethod() == ReturnRequest.RefundMethod.BANK_TRANSFER) {
            returnReq.setBankName(req.getBankName());
            returnReq.setBankAccountNumber(req.getBankAccountNumber());
            returnReq.setBankAccountName(req.getBankAccountName().trim().toUpperCase());
        }

        // Cập nhật trạng thái đơn hàng -> "Đang xử lý trả hàng"
        order.setStatus(Order.OrderStatus.RETURN_REQUESTED);
        orderRepository.save(order);

        return returnRequestRepository.save(returnReq);
    }
}