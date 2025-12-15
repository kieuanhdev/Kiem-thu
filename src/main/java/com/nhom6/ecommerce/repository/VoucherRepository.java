package com.nhom6.ecommerce.repository;

import com.nhom6.ecommerce.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    // Kiểm tra trùng mã [cite: 2364]
    boolean existsByCode(String code);

    // Tìm voucher theo mã code để áp dụng khi checkout
    Optional<Voucher> findByCode(String code);
}