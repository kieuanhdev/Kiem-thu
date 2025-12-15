package com.nhom6.ecommerce.controller;

import com.nhom6.ecommerce.repository.ProductRepository;
import com.nhom6.ecommerce.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

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
    // --- SỬA ĐOẠN NÀY ---
    @GetMapping("/checkout-page")
    public String showCheckoutPage(@RequestParam(required = false) String productId, Model model) {
        if (productId != null) {
            // Trường hợp: Mua Ngay (Buy Now) 1 sản phẩm
            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isPresent()) {
                model.addAttribute("product", productOpt.get());
                model.addAttribute("isBuyNow", true); // Cờ đánh dấu là mua ngay
            }
        } else {
            // Trường hợp: Thanh toán giỏ hàng (Cart Checkout)
            // Không truyền product xuống, Frontend sẽ tự lấy từ LocalStorage
            model.addAttribute("product", null);
            model.addAttribute("isBuyNow", false);
        }
        return "checkout";
    }
}