package com.nhom6.ecommerce.service;

import com.nhom6.ecommerce.dto.UserLoginDTO;
import com.nhom6.ecommerce.dto.UserProfileDTO;
import com.nhom6.ecommerce.dto.UserRegistrationDTO;
import com.nhom6.ecommerce.entity.User;
import com.nhom6.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Trong file UserService.java
    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
    }

    // Regex chuẩn cho email (đơn giản hóa)
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    public User registerUser(UserRegistrationDTO request) {
        // --- 1. VALIDATE EMAIL ---
        String email = request.getEmail();
        // Ereg1: Không được để trống hoặc toàn khoảng trắng
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Ereg1: Tên tài khoản không được để trống");
        }
        // Ereg3: Độ dài không quá 50 ký tự (Check trước format để tối ưu)
        if (email.length() > 50) {
            throw new RuntimeException("Ereg3: Tên tài khoản không quá 50 ký tự");
        }
        // Ereg2: Phải đúng định dạng email
        if (!Pattern.matches(EMAIL_REGEX, email)) {
            throw new RuntimeException("Ereg2: Vui lòng nhập đúng định dạng email");
        }
        // Ereg4: Email đã tồn tại
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Ereg4: Email đã được sử dụng");
        }
        // --- 2. VALIDATE MẬT KHẨU ---
        String password = request.getPassword();
        String confirmPassword = request.getConfirmPassword();
        // Erg5: Mật khẩu không được để trống
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("Erg5: Mật khẩu không được để trống hoặc toàn khoảng trắng");
        }
        // Erg6: Mật khẩu < 6 ký tự
        if (password.length() < 6) {
            throw new RuntimeException("Erg6: Mật khẩu phải có ít nhất 6 ký tự");
        }
        // Erg7: Mật khẩu > 30 ký tự
        if (password.length() > 30) {
            throw new RuntimeException("Erg7: Mật khẩu không vượt quá 30 ký tự");
        }
        // --- 3. VALIDATE XÁC NHẬN MẬT KHẨU ---
        // Erg8: Xác nhận mật khẩu để trống
        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            throw new RuntimeException("Erg8: Vui lòng nhập lại mật khẩu");
        }
        // Erg9: Mật khẩu không khớp
        if (!password.equals(confirmPassword)) {
            throw new RuntimeException("Erg9: Mật khẩu xác nhận không khớp");
        }
        // --- 4. TẠO USER MỚI (KHỞI TẠO ĐẦY ĐỦ GIÁ TRỊ MẶC ĐỊNH) ---
        User newUser = new User();
        newUser.setEmail(email);
        // Lưu ý: Cần mã hóa mật khẩu ở đây trong thực tế
        newUser.setPassword(password);
        newUser.setFullName(request.getFullName());
        newUser.setPhone(request.getPhone());
        newUser.setRole(User.Role.CUSTOMER);
        newUser.setActive(true);
        // Khởi tạo các giá trị mặc định theo Snapshot DB
        newUser.setOrderPoints(0); // oder_points mặc định 0
        newUser.setMembershipLevel(User.MembershipLevel.BRONZE); // level mặc định
        return userRepository.save(newUser);
    }

    // ... (Giữ nguyên các hàm login, updateProfile, accumulatePoints của bạn) ...

    public User login(UserLoginDTO loginDTO) {
        Optional<User> userOpt = userRepository.findByEmail(loginDTO.getEmail());
        if (userOpt.isEmpty()) throw new RuntimeException("Tài khoản không tồn tại");
        User user = userOpt.get();
        if (!user.getPassword().equals(loginDTO.getPassword())) throw new RuntimeException("Mật khẩu không chính xác");
        if (!user.isActive()) throw new RuntimeException("Tài khoản đã bị khóa");
        return user;
    }

    public User updateProfile(UserProfileDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setGender(dto.getGender());
        user.setBirthday(dto.getBirthday());
        return userRepository.save(user);
    }

    public void accumulatePoints(String userId, BigDecimal orderTotal) {
        User user = userRepository.findById(userId).orElseThrow();
        int pointsEarned = orderTotal.divide(BigDecimal.valueOf(10000)).intValue();
        int newTotalPoints = user.getOrderPoints() + pointsEarned;
        user.setOrderPoints(newTotalPoints);

        if (newTotalPoints >= 10000) user.setMembershipLevel(User.MembershipLevel.DIAMOND);
        else if (newTotalPoints >= 5000) user.setMembershipLevel(User.MembershipLevel.GOLD);
        else if (newTotalPoints >= 1000) user.setMembershipLevel(User.MembershipLevel.SILVER);
        else user.setMembershipLevel(User.MembershipLevel.BRONZE);

        userRepository.save(user);
    }


}