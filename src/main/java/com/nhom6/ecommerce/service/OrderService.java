package com.nhom6.ecommerce.service;

import com.nhom6.ecommerce.dto.CartItemDTO;
import com.nhom6.ecommerce.dto.OrderRequestDTO;
import com.nhom6.ecommerce.entity.*;
import com.nhom6.ecommerce.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private VoucherRepository voucherRepository;

    @Transactional // Đảm bảo tính toàn vẹn: Lỗi ở bất kỳ bước nào sẽ rollback toàn bộ
    public Order createOrder(OrderRequestDTO req) {
        // 1. Validate User
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        // 2. Xử lý danh sách sản phẩm & Tính tạm tính (Subtotal)
        BigDecimal subTotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        Order order = new Order(); // Tạo đối tượng Order trước để gán vào OrderItem

        for (CartItemDTO itemDTO : req.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại: " + itemDTO.getProductId()));

            // Check 2E.4: Ngừng kinh doanh (giả sử check field isActive)
            if (!product.isActive()) {
                throw new RuntimeException("Sản phẩm đã ngừng kinh doanh: " + product.getName());
            }

            // Check 2E.5: Sai lệch giá (Bảo mật giá) [cite: 126]
            if (product.getSalePrice().compareTo(itemDTO.getClientPrice()) != 0) {
                throw new RuntimeException("2E.5: Giá sản phẩm thay đổi, vui lòng tải lại trang: " + product.getName());
            }

            // Check 2E.3: Tồn kho [cite: 124]
            if (product.getStockQuantity() < itemDTO.getQuantity()) {
                throw new RuntimeException("2E.3: Sản phẩm " + product.getName() + " chỉ còn " + product.getStockQuantity());
            }

            // Trừ tồn kho [cite: 142]
            product.setStockQuantity(product.getStockQuantity() - itemDTO.getQuantity());
            productRepository.save(product);

            // Tạo OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPriceAtPurchase(product.getSalePrice()); // Snapshot giá
            orderItems.add(orderItem);

            // Cộng dồn tiền
            BigDecimal itemTotal = product.getSalePrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            subTotal = subTotal.add(itemTotal);
        }

        // 3. Xử lý Voucher (Nếu có) [cite: 101-109]
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (req.getVoucherCode() != null && !req.getVoucherCode().isEmpty()) {
            Voucher voucher = voucherRepository.findByCode(req.getVoucherCode())
                    .orElseThrow(() -> new RuntimeException("3E.1: Mã giảm giá không đúng"));

            // Validate Voucher (Hạn dùng, Số lượng, Min Order)
            if (voucher.getEndAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("3E.2: Mã giảm giá đã hết hạn");
            }
            if (voucher.getUsageLimit() <= voucher.getUsedCount()) {
                throw new RuntimeException("3E.3: Mã giảm giá đã hết lượt sử dụng");
            }
            if (subTotal.compareTo(voucher.getMinOrderValue()) < 0) {
                throw new RuntimeException("3E.4: Đơn hàng chưa đạt giá trị tối thiểu để dùng mã này");
            }

            // Tính tiền giảm
            if (voucher.getDiscountType() == Voucher.DiscountType.FIXED_AMOUNT) {
                discountAmount = voucher.getDiscountValue();
            } else if (voucher.getDiscountType() == Voucher.DiscountType.PERCENTAGE) {
                discountAmount = subTotal.multiply(voucher.getDiscountValue()).divide(BigDecimal.valueOf(100));
            }

            // Cập nhật lượt dùng Voucher [cite: 143]
            voucher.setUsedCount(voucher.getUsedCount() + 1);
            voucherRepository.save(voucher);
        }

        // 4. Tính tổng cuối cùng
        BigDecimal finalTotal = subTotal.subtract(discountAmount);
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) finalTotal = BigDecimal.ZERO;

        // Check 4E.2: Giới hạn COD > 20tr [cite: 135]
        if (req.getPaymentMethod() == Order.PaymentMethod.COD && finalTotal.compareTo(new BigDecimal("20000000")) > 0) {
            throw new RuntimeException("4E.2: Đơn hàng trên 20 triệu không hỗ trợ COD");
        }

        // 5. Lưu Order
        order.setUser(user);
        order.setRecipientName(req.getRecipientName());
        order.setPhone(req.getPhone());
        order.setAddress(req.getAddress());
        order.setPaymentMethod(req.getPaymentMethod());
        order.setTotalAmount(finalTotal);
        order.setDiscountAmount(discountAmount);
        order.setVoucherCode(req.getVoucherCode());
        order.setItems(orderItems); // Cascade sẽ tự lưu OrderItems

        return orderRepository.save(order);
    }
}