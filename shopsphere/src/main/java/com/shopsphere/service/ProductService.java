package com.shopsphere.service;

import com.shopsphere.dto.ProductRequest;
import com.shopsphere.dto.ProductResponse;
import com.shopsphere.entity.Product;
import com.shopsphere.entity.ProductStatus;
import com.shopsphere.entity.AccountRole;
import com.shopsphere.entity.AccountStatus;
import com.shopsphere.entity.AccountUser;
import com.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.repository.ProductRepository;
import com.shopsphere.repository.AccountUserRepository;
import com.shopsphere.entity.Category;
import com.shopsphere.repository.CategoryRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final AccountUserRepository accountUserRepository;

    @Autowired
    public ProductService(
        ProductRepository productRepository,
        CategoryRepository categoryRepository,
        AccountUserRepository accountUserRepository
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.accountUserRepository = accountUserRepository;
    }

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this(productRepository, categoryRepository, null);
    }

    // Get all products
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts(String email, AccountRole role) {
        List<Product> products;
        if (role == AccountRole.ADMIN) {
            products = productRepository.findAll();
        } else if (role == AccountRole.SELLER && email != null) {
            products = productRepository.findBySeller_EmailIgnoreCase(email);
        } else {
            products = productRepository.findVisibleWithStatus(ProductStatus.ACTIVE);
        }
        return products.stream().map(this::convertToResponse).toList();
    }

    // Get product by ID
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id: " + id
                        ));

        return convertToResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id, String email, AccountRole role) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        boolean owner = email != null && product.getSeller() != null
                && product.getSeller().getEmail().equalsIgnoreCase(email);
        if (role != AccountRole.ADMIN && !owner && product.getModerationStatus() != ProductStatus.ACTIVE) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        return convertToResponse(product);
    }

    // Create product
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        return createProduct(request, null, AccountRole.ADMIN);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request, String email, AccountRole role) {
        Category category = findCategoryById(request.getCategoryId());

        Product product = new Product(
                request.getName(),
                request.getPrice(),
                request.getDescription(),
                request.getStock()
        );

        product.setCategory(category);
        product.setImageUrl(normalizeImageUrl(request.getImageUrl()));
        if (role == AccountRole.SELLER) {
            AccountUser seller = findSeller(email);
            product.setSeller(seller);
            product.setModerationStatus(ProductStatus.PENDING_REVIEW);
        } else {
            product.setModerationStatus(ProductStatus.ACTIVE);
        }

        Product savedProduct = productRepository.save(product);

        return convertToResponse(savedProduct);
}

    // Update product
    @Transactional
    public ProductResponse updateProduct(
            Long id,
            ProductRequest request
    ) {
        return updateProduct(id, request, null, AccountRole.ADMIN);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request, String email, AccountRole role) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id: " + id
                        ));

        ensureCanManage(existingProduct, email, role);

        Category category = findCategoryById(request.getCategoryId());

        existingProduct.setName(request.getName());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setStock(request.getStock());
        existingProduct.setCategory(category);
        existingProduct.setImageUrl(normalizeImageUrl(request.getImageUrl()));
        if (role == AccountRole.SELLER) {
            existingProduct.setModerationStatus(ProductStatus.PENDING_REVIEW);
        }

        Product updatedProduct = productRepository.save(existingProduct);

        return convertToResponse(updatedProduct);
    }

    // Delete product
    @Transactional
    public void deleteProduct(Long id) {
        deleteProduct(id, null, AccountRole.ADMIN);
    }

    @Transactional
    public void deleteProduct(Long id, String email, AccountRole role) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Product not found with id: " + id
            );
        }

        Product product = productRepository.findById(id).orElseThrow();
        ensureCanManage(product, email, role);

        productRepository.deleteById(id);
    }

    @Transactional
    public ProductResponse moderateProduct(Long id, ProductStatus status) {
        if (status == ProductStatus.PENDING_REVIEW) {
            throw new IllegalArgumentException("Admin cannot set a product back to pending review");
        }
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        product.setModerationStatus(status);
        return convertToResponse(productRepository.save(product));
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
                category != null ? category.getName() : null,
                product.getImageUrl(),
                product.getSeller() != null ? product.getSeller().getId() : null,
                product.getSeller() != null
                        ? (product.getSeller().getStoreName() != null && !product.getSeller().getStoreName().isBlank()
                                ? product.getSeller().getStoreName() : product.getSeller().getDisplayName())
                        : null,
                product.getModerationStatus()
        );
    }

    private AccountUser findSeller(String email) {
        if (email == null) {
            throw new IllegalArgumentException("An authenticated Seller account is required");
        }
        AccountUser seller = accountUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Seller account not found"));
        if (seller.getRole() != AccountRole.SELLER || seller.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Only approved Seller accounts can manage products");
        }
        return seller;
    }

    private void ensureCanManage(Product product, String email, AccountRole role) {
        if (role == AccountRole.ADMIN) return;
        AccountUser seller = findSeller(email);
        if (product.getSeller() == null || !product.getSeller().getId().equals(seller.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("You can only manage your own products");
        }
    }

    private String normalizeImageUrl(String imageUrl) {
        return imageUrl == null || imageUrl.isBlank() ? null : imageUrl.trim();
    }

    private Category findCategoryById(Long categoryId) {
    return categoryRepository.findById(categoryId)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Category not found with id: " + categoryId
                    ));
}
}
