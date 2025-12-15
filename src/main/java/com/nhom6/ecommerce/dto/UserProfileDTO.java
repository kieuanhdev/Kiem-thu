package com.nhom6.ecommerce.dto;

import com.nhom6.ecommerce.entity.User;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UserProfileDTO {
    private String userId; // ID để định danh
    private String fullName;
    private String phone;
    private String address;
    private User.Gender gender;
    private LocalDate birthday;
}