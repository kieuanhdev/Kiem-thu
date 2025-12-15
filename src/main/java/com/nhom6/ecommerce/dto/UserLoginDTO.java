package com.nhom6.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginDTO {
    @NotBlank(message = "Vui lòng nhập Email")
    private String email;

    @NotBlank(message = "Vui lòng nhập Mật khẩu")
    private String password;
}