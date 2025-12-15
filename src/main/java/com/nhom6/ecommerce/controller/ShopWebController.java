package com.nhom6.ecommerce.controller;

import com.nhom6.ecommerce.repository.ProductRepository;
import com.nhom6.ecommerce.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ShopWebController {

    @Autowired
    private ProductRepository productRepository;

    // 1. Trang chủ (Shop): Hiển thị lưới sản phẩm
    @GetMapping("/")
    public String showShopPage(Model model) {
        // Lấy danh sách sản phẩm còn hàng và đang kinh doanh
        // (Ở đây tạm lấy tất cả, thực tế nên filter thêm isActive = true)
        model.addAttribute("products", productRepository.findAll());
        return "shop"; // Trả về shop.html
    }

    // 2. Trang Thanh toán (Checkout): Hiển thị form mua 1 sản phẩm cụ thể
    @GetMapping("/checkout-page")
    public String showCheckoutPage(@RequestParam String productId, Model model) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        model.addAttribute("product", product);
        return "checkout"; // Trả về checkout.html
    }
}