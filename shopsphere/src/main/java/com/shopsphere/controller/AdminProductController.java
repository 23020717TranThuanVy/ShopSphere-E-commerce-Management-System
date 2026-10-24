package com.shopsphere.controller;

import com.shopsphere.dto.ProductModerationRequest;
import com.shopsphere.dto.ProductResponse;
import com.shopsphere.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {
    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @PatchMapping("/{id}/moderation")
    public ProductResponse moderateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductModerationRequest request) {
        return productService.moderateProduct(id, request.getStatus());
    }
}
