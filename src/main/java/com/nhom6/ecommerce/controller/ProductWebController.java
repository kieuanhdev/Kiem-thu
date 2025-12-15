package com.nhom6.ecommerce.controller;

import com.nhom6.ecommerce.dto.ProductRequestDTO;
import com.nhom6.ecommerce.service.ProductService;
import com.nhom6.ecommerce.repository.BrandRepository;
import com.nhom6.ecommerce.repository.CategoryRepository;
import com.nhom6.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller // Dùng @Controller thay vì @RestController để trả về HTML
@RequestMapping("/admin/products")
public class ProductWebController {

    @Autowired private ProductRepository productRepository;
    @Autowired private ProductService productService;
    @Autowired private BrandRepository brandRepository;
    @Autowired private CategoryRepository categoryRepository;

    // 1. Trang danh sách sản phẩm
    @GetMapping
    public String listProducts(Model model) {
        // Lấy tất cả sản phẩm đẩy ra giao diện
        model.addAttribute("products", productRepository.findAll());
        return "product-list"; // Trả về file product-list.html
    }

    // 2. Trang form tạo mới
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("productDTO", new ProductRequestDTO());
        // Load danh sách Brand và Category để chọn trong dropdown
        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());
        return "product-form"; // Trả về file product-form.html
    }

    // 3. Xử lý lưu form
    @PostMapping("/save")
    public String saveProduct(@ModelAttribute ProductRequestDTO productDTO) {
        try {
            productService.createProduct(productDTO);
            return "redirect:/admin/products"; // Lưu xong quay về trang danh sách
        } catch (Exception e) {
            return "redirect:/admin/products/create?error=" + e.getMessage();
        }
    }
}