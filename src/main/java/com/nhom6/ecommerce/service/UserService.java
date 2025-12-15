package com.nhom6.ecommerce.service;

import com.nhom6.ecommerce.dto.UserLoginDTO;
import com.nhom6.ecommerce.dto.UserRegistrationDTO;
import com.nhom6.ecommerce.entity.User;
import com.nhom6.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
// Lưu ý: Cần thêm BCryptPasswordEncoder để mã hóa pass

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User registerUser(UserRegistrationDTO request) {
        // 1. Kiểm tra Email trùng [cite: 525]
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Ereg4: Email đã được sử dụng");
        }

        // 2. Kiểm tra xác nhận mật khẩu [cite: 539]
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Erg9: Mật khẩu xác nhận không khớp");
        }

        // 3. Tạo User mới
        User newUser = new User();
        newUser.setEmail(request.getEmail());
        // TODO: Mã hóa mật khẩu trước khi lưu (dùng BCrypt)
        newUser.setPassword(request.getPassword());
        newUser.setFullName(request.getFullName());
        newUser.setPhone(request.getPhone());
        newUser.setRole(User.Role.CUSTOMER); // Mặc định là khách hàng
        newUser.setActive(true);

        return userRepository.save(newUser);
    }

    // THÊM HÀM NÀY:
    public User login(UserLoginDTO loginDTO) {
        // 1. Tìm user theo email
        Optional<User> userOpt = userRepository.findByEmail(loginDTO.getEmail());

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Tài khoản không tồn tại");
        }

        User user = userOpt.get();

        // 2. Kiểm tra mật khẩu (Lưu ý: Nếu bạn chưa mã hóa thì so sánh string thường)
        if (!user.getPassword().equals(loginDTO.getPassword())) {
            throw new RuntimeException("Mật khẩu không chính xác");
        }

        // 3. Kiểm tra trạng thái hoạt động
        if (!user.isActive()) {
            throw new RuntimeException("Tài khoản đã bị khóa");
        }

        return user;
    }
}