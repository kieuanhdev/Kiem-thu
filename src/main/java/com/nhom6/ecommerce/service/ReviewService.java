package com.nhom6.ecommerce.service;

import com.nhom6.ecommerce.dto.ReviewRequestDTO;
import com.nhom6.ecommerce.entity.*;
import com.nhom6.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    @Autowired private ReviewRepository reviewRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;

    public Review createReview(ReviewRequestDTO req) {
        // 1. Kiểm tra sản phẩm tồn tại [cite: 651]
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new RuntimeException("Erg2: Sản phẩm không tồn tại"));

        // 2. Kiểm tra User tồn tại
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("Erg3: Người dùng không tồn tại"));

        // 3. CHECK LOGIC MUA HÀNG: User phải có đơn hàng COMPLETED chứa sản phẩm này
        // [cite: 660-661]
        Order order = orderRepository.findCompletedOrderWithProduct(req.getUserId(), req.getProductId())
                .orElseThrow(() -> new RuntimeException("Erg4/Erg5: Bạn chỉ có thể đánh giá sản phẩm đã mua và đơn hàng đã hoàn tất"));

        // 4. Kiểm tra xem đã đánh giá chưa (tránh spam 1 đơn đánh giá nhiều lần)
        if (reviewRepository.existsByOrderIdAndProductId(order.getId(), req.getProductId())) {
            throw new RuntimeException("Bạn đã đánh giá sản phẩm này cho đơn hàng này rồi.");
        }

        // 5. Lưu đánh giá
        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setOrder(order); // Gắn với đơn hàng gốc
        review.setRating(req.getRating());
        review.setContent(req.getContent());
        review.setImages(req.getImages());

        // Cập nhật lại thống kê rating cho Product (Optional - Logic nâng cao)
        // updateProductRating(product);

        return reviewRepository.save(review);
    }
}