package com.nhom6.ecommerce.service;

import com.nhom6.ecommerce.dto.ReviewRequestDTO;
import com.nhom6.ecommerce.entity.*;
import com.nhom6.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ReviewService {

    @Autowired private ReviewRepository reviewRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;

    // Cấu hình validate ảnh
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/jpg");

    @Transactional
    public Review createReview(ReviewRequestDTO req) {
        // 1. Validate Product [Erg2]
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new RuntimeException("Erg2: Sản phẩm không tồn tại"));

        // 2. Validate User [Erg3]
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("Erg3: Bạn cần đăng nhập để đánh giá sản phẩm"));

        // 3. CHECK LỊCH SỬ MUA HÀNG (Tách Erg4 và Erg5)
        List<Order> orders = orderRepository.findAllOrdersByProduct(req.getUserId(), req.getProductId());

        // [Erg4] Chưa từng mua
        if (orders.isEmpty()) {
            throw new RuntimeException("Erg4: Chỉ khách hàng đã mua sản phẩm mới có thể đánh giá");
        }

        // [Erg5] Đã mua nhưng chưa có đơn nào hoàn tất
        Order completedOrder = orders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.COMPLETED)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Erg5: Bạn chỉ có thể đánh giá sau khi đơn hàng hoàn tất"));

        // 4. Validate Duplicate (Tránh spam)
        if (reviewRepository.existsByOrderIdAndProductId(completedOrder.getId(), req.getProductId())) {
            throw new RuntimeException("Bạn đã đánh giá sản phẩm này cho đơn hàng này rồi.");
        }

        // 5. VALIDATE & UPLOAD ẢNH [Erg9, Erg10]
        List<String> uploadedUrls = new ArrayList<>();
        if (req.getImages() != null && !req.getImages().isEmpty()) {
            for (MultipartFile file : req.getImages()) {
                // Bỏ qua file rỗng nếu có
                if (file.isEmpty()) continue;

                // [Erg10] Check dung lượng > 5MB
                if (file.getSize() > MAX_FILE_SIZE) {
                    throw new RuntimeException("Erg10: Dung lượng ảnh không vượt quá 5MB (" + file.getOriginalFilename() + ")");
                }

                // [Erg9] Check định dạng
                if (!ALLOWED_TYPES.contains(file.getContentType())) {
                    throw new RuntimeException("Erg9: Ảnh phải có định dạng .jpg hoặc .png (" + file.getOriginalFilename() + ")");
                }

                // Thực hiện upload (Giả lập)
                String url = uploadFile(file);
                uploadedUrls.add(url);
            }
        }

        // 6. Lưu Review
        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setOrder(completedOrder);
        review.setRating(req.getRating());
        review.setContent(req.getContent());
        review.setImageUrls(uploadedUrls); // Lưu danh sách URL

        return reviewRepository.save(review);
    }

    // Hàm giả lập upload file lên Server/S3
    private String uploadFile(MultipartFile file) {
        // Trong thực tế: gọi AWS S3 hoặc lưu vào thư mục static
        // Ở đây trả về đường dẫn giả
        return "/uploads/reviews/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
    }
}