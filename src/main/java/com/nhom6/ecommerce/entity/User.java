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
    @GeneratedValue(strategy = GenerationType.UUID) // <--- SỬA LẠI DÒNG NÀY
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

    @Column(name = "membership_level")
    private String membershipLevel;

    private LocalDate birthday;

    @Column(name = "order_points")
    private int orderPoints = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt; // Sẽ xử lý tự động ở dưới

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Tự động cập nhật thời gian khi tạo hoặc sửa
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Enum
    public enum Gender { MALE, FEMALE, OTHER }
    public enum Role { CUSTOMER, ADMIN }
}