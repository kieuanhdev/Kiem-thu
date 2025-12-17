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
import java.util.regex.Pattern;

@Service
public class OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private VoucherRepository voucherRepository;
    @Autowired private UserService userService;

    /**
     * HÀM CHÍNH: TẠO ĐƠN HÀNG
     */
    @Transactional
    public Order createOrder(OrderRequestDTO req) {
        // 1. Validate Input
        validateRequestInputs(req);

        // 2. Validate User
        User user = validateAndGetUser(req.getUserId());

        // 3. Xử lý Sản phẩm & Tính SubTotal
        BigDecimal subTotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        Order order = new Order();

        for (CartItemDTO itemDTO : req.getItems()) {
            Product product = validateAndGetProduct(itemDTO);

            // Trừ tồn kho
            product.setStockQuantity(product.getStockQuantity() - itemDTO.getQuantity());
            productRepository.save(product);

            // Tạo OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPriceAtPurchase(product.getSalePrice());
            orderItems.add(orderItem);

            subTotal = subTotal.add(product.getSalePrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
        }

        // 4. Xử lý Voucher (Tách hàm riêng)
        BigDecimal discountAmount = calculateVoucherDiscount(req.getVoucherCode(), user, subTotal, orderItems);

        // 5. Tính tổng cuối & Validate Thanh toán (Tách hàm riêng - ĐÚNG Ý BẠN MUỐN)
        BigDecimal finalTotal = calculateFinalTotal(subTotal, discountAmount);

        validatePaymentMethod(req.getPaymentMethod(), finalTotal);

        // 6. Lưu đơn hàng
        order.setUser(user);
        order.setRecipientName(req.getRecipientName().trim());
        order.setPhone(req.getPhone().trim());
        order.setAddress(req.getAddress().trim());
        order.setPaymentMethod(req.getPaymentMethod());
        order.setTotalAmount(finalTotal);
        order.setDiscountAmount(discountAmount);
        order.setVoucherCode(req.getVoucherCode());
        order.setItems(orderItems);
        order.setStatus(Order.OrderStatus.PENDING);

        Order savedOrder = orderRepository.save(order);

        // 7. Tích điểm
        userService.accumulatePoints(user.getUserId(), finalTotal);

        return savedOrder;
    }

    // =========================================================================
    // CÁC HÀM PHỤ TRỢ (PRIVATE METHODS)
    // =========================================================================

    /**
     * Tính tổng tiền cuối cùng (Đảm bảo không âm)
     */
    private BigDecimal calculateFinalTotal(BigDecimal subTotal, BigDecimal discountAmount) {
        BigDecimal total = subTotal.subtract(discountAmount);
        return total.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : total;
    }

    /**
     * Validate phương thức thanh toán và giới hạn COD
     */
    private void validatePaymentMethod(Order.PaymentMethod paymentMethod, BigDecimal finalTotal) {
        if (paymentMethod == null) {
            throw new RuntimeException("4E.1: Vui lòng chọn phương thức thanh toán.");
        }
        // Giới hạn COD 20 triệu
        if (paymentMethod == Order.PaymentMethod.COD && finalTotal.compareTo(new BigDecimal("20000000")) > 0) {
            throw new RuntimeException("4E.2: Đơn hàng trên 20 triệu không hỗ trợ COD.");
        }
    }

    private BigDecimal calculateVoucherDiscount(String voucherCode, User user, BigDecimal subTotal, List<OrderItem> orderItems) {
        if (voucherCode == null || voucherCode.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        String code = voucherCode.trim();
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("3E.1: Mã giảm giá không đúng."));

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(voucher.getStartAt()) || now.isAfter(voucher.getEndAt())) {
            throw new RuntimeException("3E.2: Mã giảm giá chưa bắt đầu hoặc đã hết hạn.");
        }

        if (voucher.getUsageLimit() > 0 && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new RuntimeException("3E.3: Mã giảm giá đã hết lượt sử dụng.");
        }

        if (subTotal.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new RuntimeException("3E.4: Đơn hàng chưa đạt giá trị tối thiểu " + voucher.getMinOrderValue() + "đ.");
        }

        long userUsedCount = orderRepository.countVoucherUsageByUser(user.getUserId(), code);
        if (voucher.getUsageLimitPerUser() != null && userUsedCount >= voucher.getUsageLimitPerUser()) {
            throw new RuntimeException("Bạn đã sử dụng mã này quá số lần quy định (" + voucher.getUsageLimitPerUser() + " lần).");
        }

        if (!checkVoucherScope(voucher, orderItems)) {
            throw new RuntimeException("3E.5: Mã không áp dụng cho sản phẩm trong giỏ.");
        }

        if (voucher.getAudienceType() == Voucher.AudienceType.NEW_USER) {
            long totalOrders = orderRepository.countVoucherUsageByUser(user.getUserId(), null);
            if (totalOrders > 0) throw new RuntimeException("Mã này chỉ dành cho khách hàng mới.");
        }

        BigDecimal discount = BigDecimal.ZERO;
        if (voucher.getDiscountType() == Voucher.DiscountType.FIXED_AMOUNT) {
            discount = voucher.getDiscountValue();
        } else if (voucher.getDiscountType() == Voucher.DiscountType.PERCENTAGE) {
            discount = subTotal.multiply(voucher.getDiscountValue()).divide(BigDecimal.valueOf(100));
        }

        voucher.setUsedCount(voucher.getUsedCount() + 1);
        voucherRepository.save(voucher);

        return discount;
    }

    private boolean checkVoucherScope(Voucher voucher, List<OrderItem> items) {
        if (voucher.getScope() == Voucher.ScopeType.GLOBAL) return true;

        List<String> allowedIds = voucher.getScopeIds();
        for (OrderItem item : items) {
            if (voucher.getScope() == Voucher.ScopeType.PRODUCT) {
                if (allowedIds.contains(item.getProduct().getId())) return true;
            }
            if (voucher.getScope() == Voucher.ScopeType.CATEGORY) {
                if (isProductInAllowedCategories(item.getProduct(), allowedIds)) return true;
            }
        }
        return false;
    }

    private boolean isProductInAllowedCategories(Product product, List<String> allowedCategoryIds) {
        if (product.getCategories() == null) return false;
        for (Category cat : product.getCategories()) {
            if (allowedCategoryIds.contains(String.valueOf(cat.getId()))) return true;
        }
        return false;
    }

    private void validateRequestInputs(OrderRequestDTO req) {
        if (req.getRecipientName() == null || req.getPhone() == null || req.getAddress() == null) {
            throw new RuntimeException("1E.1: Vui lòng nhập đầy đủ thông tin giao hàng.");
        }
        if (!Pattern.matches("^[\\p{L} ]{2,50}$", req.getRecipientName().trim())) {
            throw new RuntimeException("1E.2: Họ tên người nhận không hợp lệ (2-50 ký tự, chỉ chứa chữ cái).");
        }
        if (!Pattern.matches("^0\\d{9}$", req.getPhone().trim())) {
            throw new RuntimeException("1E.3: Số điện thoại không hợp lệ (Phải là 10 số, bắt đầu bằng 0).");
        }
        String addr = req.getAddress().trim();
        if (addr.length() < 10 || addr.length() > 255) {
            throw new RuntimeException("1E.4: Địa chỉ quá ngắn hoặc quá dài (10-255 ký tự).");
        }
        if (addr.matches(".*[<>].*")) {
            throw new RuntimeException("Địa chỉ chứa ký tự không hợp lệ.");
        }
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new RuntimeException("2E.1: Giỏ hàng trống.");
        }
        if (req.getItems().size() > 50) {
            throw new RuntimeException("Đơn hàng không được vượt quá 50 loại sản phẩm.");
        }
    }

    private User validateAndGetUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại."));
        if (!user.isActive()) throw new RuntimeException("Tài khoản đang bị khóa.");
        if (user.getRole() != User.Role.CUSTOMER) {
            throw new RuntimeException("Tài khoản quản trị không được phép đặt hàng.");
        }
        return user;
    }

    private Product validateAndGetProduct(CartItemDTO itemDTO) {
        Product product = productRepository.findById(itemDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại (ID: " + itemDTO.getProductId() + ")"));

        if (!product.isActive()) {
            throw new RuntimeException("2E.4: Sản phẩm " + product.getName() + " đã ngừng kinh doanh.");
        }
        if (product.getStockQuantity() == 0) {
            throw new RuntimeException("2E.2: Sản phẩm " + product.getName() + " đã hết hàng.");
        }
        if (product.getStockQuantity() < itemDTO.getQuantity()) {
            throw new RuntimeException("2E.3: Sản phẩm " + product.getName() + " chỉ còn " + product.getStockQuantity() + " sản phẩm.");
        }
        if (product.getSalePrice().compareTo(itemDTO.getClientPrice()) != 0) {
            throw new RuntimeException("2E.5: Giá sản phẩm " + product.getName() + " thay đổi, vui lòng tải lại trang.");
        }
        return product;
    }
}