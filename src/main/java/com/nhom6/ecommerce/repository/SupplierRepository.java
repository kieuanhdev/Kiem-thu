package com.nhom6.ecommerce.repository;

import com.nhom6.ecommerce.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, String> {
    // JpaRepository đã có sẵn hàm existsById(String id) nên không cần viết thêm.
}