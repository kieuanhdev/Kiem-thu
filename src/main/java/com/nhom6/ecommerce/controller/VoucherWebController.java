package com.nhom6.ecommerce.controller;

import com.nhom6.ecommerce.dto.VoucherRequestDTO;
import com.nhom6.ecommerce.entity.Voucher;
import com.nhom6.ecommerce.repository.VoucherRepository;
import com.nhom6.ecommerce.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/vouchers")
public class VoucherWebController {

    @Autowired private VoucherRepository voucherRepository;
    @Autowired private VoucherService voucherService;

    // 1. Danh sách Voucher
    @GetMapping
    public String listVouchers(Model model) {
        model.addAttribute("vouchers", voucherRepository.findAll());
        return "voucher-list"; // voucher-list.html
    }

    // 2. Form tạo mới
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("voucherDTO", new VoucherRequestDTO());
        // Truyền Enum xuống View để hiển thị trong Select Option
        model.addAttribute("discountTypes", Voucher.DiscountType.values());
        model.addAttribute("scopeTypes", Voucher.ScopeType.values());
        return "voucher-form"; // voucher-form.html
    }

    // 3. Xử lý lưu
    @PostMapping("/save")
    public String saveVoucher(@ModelAttribute VoucherRequestDTO voucherDTO) {
        try {
            voucherService.createVoucher(voucherDTO);
            return "redirect:/admin/vouchers";
        } catch (Exception e) {
            // Nếu lỗi, quay lại form và kèm thông báo lỗi trên URL
            return "redirect:/admin/vouchers/create?error=" + e.getMessage();
        }
    }
}