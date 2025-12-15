package com.nhom6.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "reviews")
@Data
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người đánh giá [cite: 653]
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Sản phẩm được đánh giá [cite: 644]
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore // Tránh vòng lặp vô hạn khi trả về JSON
    private Product product;

    // Liên kết với đơn hàng để chứng minh đã mua (Optional nhưng nên có)
    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order;

    // Số sao [cite: 662-669]
    @Column(nullable = false)
    private Integer rating;

    // Nội dung [cite: 670-676]
    @Column(length = 500)
    private String content;

    // Hình ảnh đánh giá (Tối đa 5 ảnh) [cite: 677-686]
    @ElementCollection
    @CollectionTable(name = "review_images", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "image_url")
    private List<String> images;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}