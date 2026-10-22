package com.shopsphere.service;

import com.shopsphere.dto.ProductRequest;
import com.shopsphere.dto.ProductResponse;
import com.shopsphere.entity.Product;
import com.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.repository.ProductRepository;
import com.shopsphere.entity.Category;
import com.shopsphere.repository.CategoryRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(
        ProductRepository productRepository,
        CategoryRepository categoryRepository
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    // Get all products
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Get product by ID
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id: " + id
                        ));

        return convertToResponse(product);
    }

    // Create product
    public ProductResponse createProduct(ProductRequest request) {
        Category category = findCategoryById(request.getCategoryId());

        Product product = new Product(
                request.getName(),
                request.getPrice(),
                request.getDescription(),
                request.getStock()
        );

        product.setCategory(category);

        Product savedProduct = productRepository.save(product);

        return convertToResponse(savedProduct);
}

    // Update product
    public ProductResponse updateProduct(
            Long id,
            ProductRequest request
    ) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id: " + id
                        ));

        Category category = findCategoryById(request.getCategoryId());

        existingProduct.setName(request.getName());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setStock(request.getStock());
        existingProduct.setCategory(category);

        Product updatedProduct = productRepository.save(existingProduct);

        return convertToResponse(updatedProduct);
    }

    // Delete product
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Product not found with id: " + id
            );
        }

        productRepository.deleteById(id);
    }

    // Convert Entity to Response DTO
    private ProductResponse convertToResponse(Product product) {
        Category category = product.getCategory();

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.getStock(),
                category != null ? category.getId() : null,
                category != null ? category.getName() : null
        );
    }

    private Category findCategoryById(Long categoryId) {
    return categoryRepository.findById(categoryId)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Category not found with id: " + categoryId
                    ));
}
}
