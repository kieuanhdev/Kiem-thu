package com.nhom6.ecommerce.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "suppliers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Hoặc String thường tùy DB của bạn
    @Column(name = "supplier_id")
    private String id;

    @Column(nullable = false)
    private String name;

    private String contactEmail;

    private String phone;

    private String address;

    @Column(name = "is_active")
    private boolean isActive = true;
}