package com.nhom6.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private String userId; //

    @Column(unique = true, nullable = false, length = 50)
    private String email; //

    @Column(nullable = false)
    @JsonIgnore
    private String password; //

    @Column(name = "fullname", length = 50)
    private String fullName; //

    @Enumerated(EnumType.STRING)
    private Gender gender; //

    @Column(length = 10)
    private String phone; //

    @Enumerated(EnumType.STRING)
    private Role role; //

    @Column(name = "is_active")
    private boolean isActive = true; //

    private String address; //

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_level")
    private MembershipLevel membershipLevel = MembershipLevel.BRONZE; //

    private LocalDate birthday; //

    @Column(name = "order_points")
    private Integer orderPoints = 0; //

    // --- BỔ SUNG CÁC TRƯỜNG THIẾU ---

    // 1. cart_id
    // Trong thực tế Spring Boot, ta thường dùng @OneToOne với Entity Cart.
    // Nhưng để bám sát đặc tả "cart_id (varchar)", ta khai báo như sau:
    @Column(name = "cart_id")
    private String cartId;

    // 2. product_id
    // (Lưu ý: Đặc tả ghi là "Tên loại h...", có thể là sản phẩm yêu thích hoặc gợi ý)
    @Column(name = "product_id")
    private String productId;

    // --------------------------------

    @Column(name = "created_at")
    private LocalDateTime createdAt; //

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; //

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Enum Definitions
    public enum Gender { MALE, FEMALE, OTHER }
    public enum Role { CUSTOMER, ADMIN }
    public enum MembershipLevel { BRONZE, SILVER, GOLD, DIAMOND }
}