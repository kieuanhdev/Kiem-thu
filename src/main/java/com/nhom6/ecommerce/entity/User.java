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
    private String userId;

    @Column(unique = true, nullable = false, length = 50)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(name = "fullname", length = 50)
    private String fullName;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(length = 10)
    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "is_active")
    private boolean isActive = true;

    private String address;

    // --- SỬA ĐOẠN NÀY ---
    // Đổi từ String sang Enum để quản lý chặt chẽ các hạng (Đồng, Bạc, Vàng...)
    @Enumerated(EnumType.STRING)
    @Column(name = "membership_level")
    private MembershipLevel membershipLevel = MembershipLevel.BRONZE; // Mặc định là Đồng

    private LocalDate birthday;

    @Column(name = "order_points")
    private Integer orderPoints = 0; // Dùng Integer thay vì int để tương thích tốt hơn với Hibernate

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Tự động cập nhật thời gian
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- CÁC ENUM ---
    public enum Gender { MALE, FEMALE, OTHER }
    public enum Role { CUSTOMER, ADMIN }

    // Thêm Enum này để phục vụ tính năng Hạng thành viên
    public enum MembershipLevel {
        BRONZE,  // Đồng
        SILVER,  // Bạc
        GOLD,    // Vàng
        DIAMOND  // Kim cương
    }
}