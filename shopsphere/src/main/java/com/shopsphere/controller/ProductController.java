package com.shopsphere.controller;

import com.shopsphere.dto.ProductRequest;
import com.shopsphere.dto.ProductResponse;
import com.shopsphere.service.ProductService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.core.Authentication;
import com.shopsphere.entity.AccountRole;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponse> getAllProducts(Authentication authentication) {
        return productService.getAllProducts(email(authentication), role(authentication));
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable Long id, Authentication authentication) {
        return productService.getProductById(id, email(authentication), role(authentication));
    }

    @PostMapping
    public ProductResponse createProduct(
            @Valid @RequestBody ProductRequest request, Authentication authentication) {
        return productService.createProduct(request, email(authentication), role(authentication));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id, Authentication authentication) {
        productService.deleteProduct(id, email(authentication), role(authentication));
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request, Authentication authentication) {
        return productService.updateProduct(id, request, email(authentication), role(authentication));
    }

    private String email(Authentication authentication) {
        return authentication == null || "anonymousUser".equals(authentication.getPrincipal())
                ? null : authentication.getName();
    }

    private AccountRole role(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) return null;
        return authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
                .map(value -> {
                    try { return AccountRole.valueOf(value); }
                    catch (IllegalArgumentException ignored) { return null; }
                })
                .filter(java.util.Objects::nonNull)
                .findFirst().orElse(null);
    }
}
