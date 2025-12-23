package com.nhom6.ecommerce;

import com.nhom6.ecommerce.entity.*;
import com.nhom6.ecommerce.repository.*;
import com.nhom6.ecommerce.service.OrderService;
import com.nhom6.ecommerce.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceVoucherTest {

    @Mock private VoucherRepository voucherRepository;
    @Mock private OrderRepository orderRepository;

    // Inject các mock khác để OrderService khởi tạo được (dù hàm này ko dùng tới)
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserService userService;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private List<OrderItem> orderItems;
    private Voucher voucher;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId("U1");

        // Tạo 1 item mặc định
        Product p = new Product();
        p.setId("P1");
        p.setCategories(Collections.emptySet()); // Tránh null pointer khi check scope

        OrderItem item = new OrderItem();
        item.setProduct(p);
        orderItems = new ArrayList<>();
        orderItems.add(item);

        // Tạo Voucher mặc định (Hợp lệ)
        voucher = new Voucher();
        voucher.setCode("VOUCHER_TEST");
        voucher.setStartAt(LocalDateTime.now().minusDays(1));
        voucher.setEndAt(LocalDateTime.now().plusDays(1));
        voucher.setUsageLimit(100);
        voucher.setUsedCount(0);
        voucher.setMinOrderValue(BigDecimal.ZERO);
        voucher.setScope(Voucher.ScopeType.GLOBAL); // Global để pass check scope
        voucher.setAudienceType(Voucher.AudienceType.ALL);
        voucher.setDiscountType(Voucher.DiscountType.FIXED_AMOUNT);
        voucher.setDiscountValue(new BigDecimal("10000"));
    }

    /**
     * Helper method để gọi hàm private bằng Reflection
     */
    private BigDecimal invokeCalculateDiscount(String code, User u, BigDecimal subTotal, List<OrderItem> items) throws Exception {
        Method method = OrderService.class.getDeclaredMethod("calculateVoucherDiscount", String.class, User.class, BigDecimal.class, List.class);
        method.setAccessible(true);
        try {
            return (BigDecimal) method.invoke(orderService, code, u, subTotal, items);
        } catch (java.lang.reflect.InvocationTargetException e) {
            // Ném ra exception thực sự bên trong hàm để assertThrows bắt được
            throw (Exception) e.getCause();
        }
    }

    // =========================================================================
    // PHẦN 1: TEST CASES THEO ĐỘ ĐO C2 (BRANCH COVERAGE)
    // =========================================================================

    @Test
    @DisplayName("C2_TC1: Code null -> Return 0")
    void test_C2_TC1() throws Exception {
        BigDecimal result = invokeCalculateDiscount(null, user, BigDecimal.valueOf(100000), orderItems);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    @DisplayName("C2_TC2: Code không tồn tại -> Exception 3E.1")
    void test_C2_TC2() {
        when(voucherRepository.findByCode("ABC")).thenReturn(Optional.empty());

        Exception e = assertThrows(RuntimeException.class, () ->
                invokeCalculateDiscount("ABC", user, BigDecimal.TEN, orderItems));
        assertEquals("3E.1: Mã giảm giá không đúng.", e.getMessage());
    }

    @Test
    @DisplayName("C2_TC3: Chưa đến ngày bắt đầu -> Exception 3E.2")
    void test_C2_TC3() {
        voucher.setCode("V1");
        voucher.setStartAt(LocalDateTime.now().plusDays(1)); // Tương lai
        when(voucherRepository.findByCode("V1")).thenReturn(Optional.of(voucher));

        Exception e = assertThrows(RuntimeException.class, () ->
                invokeCalculateDiscount("V1", user, BigDecimal.TEN, orderItems));
        assertEquals("3E.2: Mã giảm giá chưa bắt đầu hoặc đã hết hạn.", e.getMessage());
    }

    @Test
    @DisplayName("C2_TC4: Hết lượt sử dụng Global -> Exception 3E.3")
    void test_C2_TC4() {
        voucher.setCode("V2");
        voucher.setUsageLimit(100);
        voucher.setUsedCount(100); // Full
        when(voucherRepository.findByCode("V2")).thenReturn(Optional.of(voucher));

        Exception e = assertThrows(RuntimeException.class, () ->
                invokeCalculateDiscount("V2", user, BigDecimal.TEN, orderItems));
        assertEquals("3E.3: Mã giảm giá đã hết lượt sử dụng.", e.getMessage());
    }

    @Test
    @DisplayName("C2_TC5: Chưa đủ Min Order -> Exception 3E.4")
    void test_C2_TC5() {
        voucher.setCode("V3");
        voucher.setMinOrderValue(new BigDecimal("100000")); // Min 100k
        when(voucherRepository.findByCode("V3")).thenReturn(Optional.of(voucher));

        // Subtotal 50k < 100k
        Exception e = assertThrows(RuntimeException.class, () ->
                invokeCalculateDiscount("V3", user, new BigDecimal("50000"), orderItems));
        assertTrue(e.getMessage().contains("3E.4"));
    }

    @Test
    @DisplayName("C2_TC6: Hết lượt User -> Exception")
    void test_C2_TC6() {
        voucher.setCode("V4");
        voucher.setUsageLimitPerUser(1);
        when(voucherRepository.findByCode("V4")).thenReturn(Optional.of(voucher));

        // Mock user đã dùng 1 lần
        when(orderRepository.countVoucherUsageByUser("U1", "V4")).thenReturn(1L);

        Exception e = assertThrows(RuntimeException.class, () ->
                invokeCalculateDiscount("V4", user, new BigDecimal("200000"), orderItems));
        assertTrue(e.getMessage().contains("Bạn đã sử dụng mã này quá số lần quy định"));
    }

    @Test
    @DisplayName("C2_TC7: Sai Scope -> Exception 3E.5")
    void test_C2_TC7() {
        voucher.setCode("V5");
        voucher.setScope(Voucher.ScopeType.PRODUCT);
        voucher.setScopeIds(List.of("OTHER_PRODUCT")); // Không khớp với P1 trong giỏ
        when(voucherRepository.findByCode("V5")).thenReturn(Optional.of(voucher));

        Exception e = assertThrows(RuntimeException.class, () ->
                invokeCalculateDiscount("V5", user, new BigDecimal("200000"), orderItems));
        assertEquals("3E.5: Mã không áp dụng cho sản phẩm trong giỏ.", e.getMessage());
    }

    @Test
    @DisplayName("C2_TC8: Khách cũ dùng mã New User -> Exception")
    void test_C2_TC8() {
        // 1. Setup Voucher
        voucher.setCode("V6-NEW");
        voucher.setAudienceType(Voucher.AudienceType.NEW_USER);
        when(voucherRepository.findByCode("V6-NEW")).thenReturn(Optional.of(voucher));

        // 2. Setup Mock (QUAN TRỌNG)

        // --- DÒNG BỊ THIẾU (Nguyên nhân gây lỗi) ---
        // Giả lập bước kiểm tra giới hạn sử dụng (Trạm 1)
        // Trả về 0 -> User chưa dùng mã này lần nào -> Qua cửa này
        when(orderRepository.countVoucherUsageByUser("U1", "V6-NEW")).thenReturn(0L);
        // ------------------------------------------

        // Giả lập bước kiểm tra lịch sử mua hàng (Trạm 2)
        // Trả về 5 -> User đã mua 5 đơn -> Chặn lại ở cửa này
        when(orderRepository.countVoucherUsageByUser("U1", null)).thenReturn(5L);

        // 3. Thực thi
        Exception e = assertThrows(RuntimeException.class, () ->
                invokeCalculateDiscount("V6-NEW", user, new BigDecimal("200000"), orderItems));

        // 4. Kiểm tra thông báo lỗi
        assertEquals("Mã này chỉ dành cho khách hàng mới.", e.getMessage());
    }

    @Test
    @DisplayName("C2_TC9: Happy Path Fixed Amount")
    void test_C2_TC9() throws Exception {
        voucher.setCode("V7-FIX");
        voucher.setDiscountType(Voucher.DiscountType.FIXED_AMOUNT);
        voucher.setDiscountValue(new BigDecimal("30000"));
        when(voucherRepository.findByCode("V7-FIX")).thenReturn(Optional.of(voucher));

        BigDecimal result = invokeCalculateDiscount("V7-FIX", user, new BigDecimal("200000"), orderItems);

        assertEquals(new BigDecimal("30000"), result);
        verify(voucherRepository, times(1)).save(voucher); // Verify update usage count
    }

    @Test
    @DisplayName("C2_TC10: Happy Path Percentage for NEW_USER")
    void test_C2_TC10() throws Exception {
        // 1. Setup dữ liệu Voucher
        voucher.setCode("V8-PER");
        voucher.setAudienceType(Voucher.AudienceType.NEW_USER);
        voucher.setDiscountType(Voucher.DiscountType.PERCENTAGE);
        voucher.setDiscountValue(new BigDecimal("10"));

        when(voucherRepository.findByCode("V8-PER")).thenReturn(Optional.of(voucher));

        // 2. Setup Mock (Giả lập)

        // [QUAN TRỌNG - Bị thiếu cái này]: Giả lập Trạm 1 (Check giới hạn dùng mã)
        // Ý nghĩa: User U1 chưa từng dùng mã V8-PER này lần nào (trả về 0)
        when(orderRepository.countVoucherUsageByUser("U1", "V8-PER")).thenReturn(0L);

        // Giả lập Trạm 2 (Check khách mới)
        // Ý nghĩa: User U1 chưa từng mua đơn nào (trả về 0)
        when(orderRepository.countVoucherUsageByUser("U1", null)).thenReturn(0L);

        // 3. Gọi hàm cần test
        BigDecimal result = invokeCalculateDiscount("V8-PER", user, new BigDecimal("200000"), orderItems);

        // 4. Kiểm tra kết quả
        assertEquals(0, new BigDecimal("20000").compareTo(result), "Giá trị giảm giá phải là 20,000");
    }

    @Test
    @DisplayName("C2_TC11: Unknown Type -> Return 0")
    void test_C2_TC11() throws Exception {
        voucher.setCode("V9-ERR");
        voucher.setDiscountType(null); // Loại không xác định
        when(voucherRepository.findByCode("V9-ERR")).thenReturn(Optional.of(voucher));

        BigDecimal result = invokeCalculateDiscount("V9-ERR", user, new BigDecimal("200000"), orderItems);
        assertEquals(BigDecimal.ZERO, result);
    }

    // =========================================================================
    // PHẦN 2: TEST CASES THEO ĐỘ ĐO C3 (CONDITION COVERAGE)
    // Các TC này tập trung vào các điều kiện phức hợp (AND/OR)
    // =========================================================================

    @Test
    @DisplayName("C3_TC2: Code rỗng (Empty string) -> Return 0")
    void test_C3_TC2() throws Exception {
        // a(S), b(Đ)
        BigDecimal result = invokeCalculateDiscount("   ", user, BigDecimal.ZERO, orderItems);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    @DisplayName("C3_TC4: Đã hết hạn (Expired) -> Exception 3E.2")
    void test_C3_TC4() {
        // c(S), d(Đ)
        voucher.setCode("V1");
        voucher.setStartAt(LocalDateTime.now().minusDays(10));
        voucher.setEndAt(LocalDateTime.now().minusDays(1)); // Đã hết hạn
        when(voucherRepository.findByCode("V1")).thenReturn(Optional.of(voucher));

        Exception e = assertThrows(RuntimeException.class, () ->
                invokeCalculateDiscount("V1", user, BigDecimal.TEN, orderItems));
        assertEquals("3E.2: Mã giảm giá chưa bắt đầu hoặc đã hết hạn.", e.getMessage());
    }

    @Test
    @DisplayName("C3_TC5: e(Đ), f(Đ) -> Có limit tổng và Đã hết -> Exception 3E.3")
    void test_C3_TC5() {
        voucher.setCode("V2");
        voucher.setUsageLimit(10);
        voucher.setUsedCount(10);
        when(voucherRepository.findByCode("V2")).thenReturn(Optional.of(voucher));

        Exception e = assertThrows(RuntimeException.class, () ->
                invokeCalculateDiscount("V2", user, BigDecimal.TEN, orderItems));
        assertEquals("3E.3: Mã giảm giá đã hết lượt sử dụng.", e.getMessage());
    }

    @Test
    @DisplayName("C3_TC6: e(Đ), f(S), y(S) -> Còn lượt tổng, Không limit User -> Exception Scope (Fail sau)")
    void test_C3_TC6() {
        voucher.setCode("V3");
        voucher.setUsageLimit(100);
        voucher.setUsedCount(1); // Còn lượt
        voucher.setUsageLimitPerUser(null); // Không limit user

        // Cố tình làm sai Scope để check việc nó vượt qua được check limit user
        voucher.setScope(Voucher.ScopeType.PRODUCT);
        voucher.setScopeIds(List.of("OTHER"));

        when(voucherRepository.findByCode("V3")).thenReturn(Optional.of(voucher));

        Exception e = assertThrows(RuntimeException.class, () ->
                invokeCalculateDiscount("V3", user, new BigDecimal("200000"), orderItems));
        // Mong đợi lỗi Scope, chứng tỏ đã qua được bước check User Limit
        assertEquals("3E.5: Mã không áp dụng cho sản phẩm trong giỏ.", e.getMessage());
    }

    @Test
    @DisplayName("C3_TC7: y(Đ), h(S) -> Có limit User, Còn lượt User -> Exception Scope")
    void test_C3_TC7() {
        voucher.setCode("V4");
        voucher.setUsageLimitPerUser(5);
        when(voucherRepository.findByCode("V4")).thenReturn(Optional.of(voucher));

        // Mock user mới dùng 1 lần (1 < 5) -> Còn lượt
        when(orderRepository.countVoucherUsageByUser("U1", "V4")).thenReturn(1L);

        // Sai Scope
        voucher.setScope(Voucher.ScopeType.PRODUCT);
        voucher.setScopeIds(List.of("OTHER"));

        Exception e = assertThrows(RuntimeException.class, () ->
                invokeCalculateDiscount("V4", user, new BigDecimal("200000"), orderItems));
        assertEquals("3E.5: Mã không áp dụng cho sản phẩm trong giỏ.", e.getMessage());
    }

    @Test
    @DisplayName("C3_TC8: y(Đ), h(Đ) -> Có limit User, Hết lượt User -> Exception User Limit")
    void test_C3_TC8() {
        voucher.setCode("V4");
        voucher.setUsageLimitPerUser(5);
        when(voucherRepository.findByCode("V4")).thenReturn(Optional.of(voucher));

        // Mock user đã dùng 5 lần (5 >= 5) -> Hết lượt
        when(orderRepository.countVoucherUsageByUser("U1", "V4")).thenReturn(5L);

        Exception e = assertThrows(RuntimeException.class, () ->
                invokeCalculateDiscount("V4", user, new BigDecimal("200000"), orderItems));
        assertTrue(e.getMessage().contains("Bạn đã sử dụng mã này quá số lần quy định"));
    }
}