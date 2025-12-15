package com.nhom6.ecommerce.repository;

import com.nhom6.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    // Kiểm tra trùng email [cite: 525]
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
}