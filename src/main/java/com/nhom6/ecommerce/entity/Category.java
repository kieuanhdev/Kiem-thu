package com.nhom6.ecommerce.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "categories")
@Data
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String code; // Mã danh mục

    @Column(name = "is_active")
    private boolean isActive = true;
}