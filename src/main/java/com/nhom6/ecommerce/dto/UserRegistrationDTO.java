package com.nhom6.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationDTO {

    // [cite: 515-525] Validate Email
    @NotBlank(message = "Ereg1: Tên tài khoản không được để trống")
    @Email(message = "Ereg2: Vui lòng nhập đúng định dạng email")
    @Size(max = 50, message = "Ereg3: Tên tài khoản không quá 50 ký tự")
    private String email;

    // [cite: 526-531] Validate Password
    @NotBlank(message = "Erg5: Mật khẩu không được để trống hoặc toàn khoảng trắng")
    @Size(min = 6, message = "Erg6: Mật khẩu phải có ít nhất 6 ký tự")
    @Size(max = 30, message = "Erg7: Mật khẩu không vượt quá 30 ký tự")
    private String password;

    // [cite: 532-539] Validate Confirm Password
    @NotBlank(message = "Erg8: Vui lòng nhập lại mật khẩu")
    private String confirmPassword;

    private String fullName;
    private String phone;
}