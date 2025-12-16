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
import java.util.Set;
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
     * Luồng xử lý chính được quy hoạch rõ ràng từng bước.
     */
    @Transactional
    public Order createOrder(OrderRequestDTO req) {
        // 1. Validate dữ liệu đầu vào (Tên, SĐT, Địa chỉ...)
        validateRequestInputs(req);

        // 2. Lấy & Validate User
        User user = validateAndGetUser(req.getUserId());

        // 3. Xử lý Sản phẩm (Loop) & Tính tạm tính
        BigDecimal subTotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        Order order = new Order(); // Object tạm để gán quan hệ

        for (CartItemDTO itemDTO : req.getItems()) {
            // Tách logic kiểm tra sản phẩm ra hàm riêng
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

            // Cộng dồn tiền
            subTotal = subTotal.add(product.getSalePrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
        }

        // 4. Xử lý Voucher (Nếu có)
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (req.getVoucherCode() != null && !req.getVoucherCode().isEmpty()) {
            // Tách logic Voucher phức tạp ra hàm riêng
            discountAmount = processVoucherAndGetDiscount(req.getVoucherCode(), user, subTotal, orderItems);
        }

        // 5. Tính tổng cuối & Kiểm tra Thanh toán
        BigDecimal finalTotal = subTotal.subtract(discountAmount);
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) finalTotal = BigDecimal.ZERO;

        if (req.getPaymentMethod() == null) {
            throw new RuntimeException("4E.1: Vui lòng chọn phương thức thanh toán.");
        }
        // Check COD giới hạn 20tr
        if (req.getPaymentMethod() == Order.PaymentMethod.COD && finalTotal.compareTo(new BigDecimal("20000000")) > 0) {
            throw new RuntimeException("4E.2: Đơn hàng trên 20 triệu không hỗ trợ COD.");
        }

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

        // 7. Tích điểm & Thăng hạng
        userService.accumulatePoints(user.getUserId(), finalTotal);

        return savedOrder;
    }

    // =========================================================================
    // CÁC HÀM PHỤ TRỢ (PRIVATE METHODS) - GIÚP CODE GỌN GÀNG
    // =========================================================================

    /**
     * Validate định dạng đầu vào (Regex, Length...)
     */
    private void validateRequestInputs(OrderRequestDTO req) {
        // [1E.1]
        if (req.getRecipientName() == null || req.getPhone() == null || req.getAddress() == null) {
            throw new RuntimeException("1E.1: Vui lòng nhập đầy đủ thông tin giao hàng.");
        }
        // [1E.2] Tên
        if (!Pattern.matches("^[\\p{L} ]{2,50}$", req.getRecipientName().trim())) {
            throw new RuntimeException("1E.2: Họ tên người nhận không hợp lệ (2-50 ký tự, chỉ chứa chữ cái).");
        }
        // [1E.3] SĐT
        if (!Pattern.matches("^0\\d{9}$", req.getPhone().trim())) {
            throw new RuntimeException("1E.3: Số điện thoại không hợp lệ (Phải là 10 số, bắt đầu bằng 0).");
        }
        // [1E.4] Địa chỉ
        String addr = req.getAddress().trim();
        if (addr.length() < 10 || addr.length() > 255) {
            throw new RuntimeException("1E.4: Địa chỉ quá ngắn hoặc quá dài (10-255 ký tự).");
        }
        if (addr.matches(".*[<>].*")) {
            throw new RuntimeException("Địa chỉ chứa ký tự không hợp lệ.");
        }
        // [2E.1] Giỏ hàng
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new RuntimeException("2E.1: Giỏ hàng trống.");
        }
        if (req.getItems().size() > 50) {
            throw new RuntimeException("Đơn hàng không được vượt quá 50 loại sản phẩm.");
        }
    }

    /**
     * Lấy User và kiểm tra quyền hạn
     */
    private User validateAndGetUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại."));
        if (!user.isActive()) throw new RuntimeException("Tài khoản đang bị khóa.");
        if (user.getRole() != User.Role.CUSTOMER) {
            throw new RuntimeException("Tài khoản quản trị không được phép đặt hàng.");
        }
        return user;
    }

    /**
     * Tìm Product, kiểm tra Active, Stock, Price
     */
    private Product validateAndGetProduct(CartItemDTO itemDTO) {
        Product product = productRepository.findById(itemDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại (ID: " + itemDTO.getProductId() + ")"));

        // [2E.4]
        if (!product.isActive()) {
            throw new RuntimeException("2E.4: Sản phẩm " + product.getName() + " đã ngừng kinh doanh.");
        }
        // [2E.2, 2E.3]
        if (product.getStockQuantity() == 0) {
            throw new RuntimeException("2E.2: Sản phẩm " + product.getName() + " đã hết hàng.");
        }
        if (product.getStockQuantity() < itemDTO.getQuantity()) {
            throw new RuntimeException("2E.3: Sản phẩm " + product.getName() + " chỉ còn " + product.getStockQuantity() + " sản phẩm.");
        }
        // [2E.5]
        if (product.getSalePrice().compareTo(itemDTO.getClientPrice()) != 0) {
            throw new RuntimeException("2E.5: Giá sản phẩm " + product.getName() + " thay đổi, vui lòng tải lại trang.");
        }
        return product;
    }

    /**
     * Xử lý toàn bộ logic Voucher: Validate & Tính tiền giảm & Update lượt dùng
     */
    private BigDecimal processVoucherAndGetDiscount(String voucherCode, User user, BigDecimal subTotal, List<OrderItem> orderItems) {
        String code = voucherCode.trim();
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("3E.1: Mã giảm giá không đúng."));

        // Validate cơ bản
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

        // Validate nâng cao (Personal Limit)
        long userUsedCount = orderRepository.countVoucherUsageByUser(user.getUserId(), code);
        if (voucher.getUsageLimitPerUser() != null && userUsedCount >= voucher.getUsageLimitPerUser()) {
            throw new RuntimeException("Bạn đã sử dụng mã này quá số lần quy định (" + voucher.getUsageLimitPerUser() + " lần).");
        }

        // Validate Scope
        if (!checkVoucherScope(voucher, orderItems)) {
            throw new RuntimeException("3E.5: Mã không áp dụng cho sản phẩm trong giỏ.");
        }

        // Validate Audience (New User)
        if (voucher.getAudienceType() == Voucher.AudienceType.NEW_USER) {
            long totalOrders = orderRepository.countVoucherUsageByUser(user.getUserId(), null);
            if (totalOrders > 0) throw new RuntimeException("Mã này chỉ dành cho khách hàng mới.");
        }

        // Tính tiền giảm
        BigDecimal discount = BigDecimal.ZERO;
        if (voucher.getDiscountType() == Voucher.DiscountType.FIXED_AMOUNT) {
            discount = voucher.getDiscountValue();
        } else if (voucher.getDiscountType() == Voucher.DiscountType.PERCENTAGE) {
            discount = subTotal.multiply(voucher.getDiscountValue()).divide(BigDecimal.valueOf(100));
        }

        // Cập nhật lượt dùng
        voucher.setUsedCount(voucher.getUsedCount() + 1);
        voucherRepository.save(voucher);

        return discount;
    }

    /**
     * Kiểm tra Phạm vi áp dụng (Scope)
     */
    private boolean checkVoucherScope(Voucher voucher, List<OrderItem> items) {
        if (voucher.getScope() == Voucher.ScopeType.GLOBAL) return true;

        List<String> allowedIds = voucher.getScopeIds();

        if (voucher.getScope() == Voucher.ScopeType.PRODUCT) {
            for (OrderItem item : items) {
                if (allowedIds.contains(item.getProduct().getId())) return true;
            }
        } else if (voucher.getScope() == Voucher.ScopeType.CATEGORY) {
            for (OrderItem item : items) {
                Set<Category> productCategories = item.getProduct().getCategories();
                if (productCategories != null) {
                    for (Category cat : productCategories) {
                        if (allowedIds.contains(String.valueOf(cat.getId()))) return true;
                    }
                }
            }
        }
        return false;
    }
}