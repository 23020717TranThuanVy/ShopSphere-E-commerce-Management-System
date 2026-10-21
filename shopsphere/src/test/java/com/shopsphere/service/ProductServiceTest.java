package com.shopsphere.service;

import com.shopsphere.dto.ProductResponse;
import com.shopsphere.entity.Category;
import com.shopsphere.entity.Product;
import com.shopsphere.repository.CategoryRepository;
import com.shopsphere.repository.ProductRepository;
import com.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.dto.ProductRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void getAllProducts_shouldReturnProductList() {
        // Arrange
        Product product = new Product(
                "Laptop",
                new BigDecimal("15000000"),
                "Gaming laptop",
                10
        );

        when(productRepository.findAll())
                .thenReturn(List.of(product));

        // Act
        List<ProductResponse> result =
                productService.getAllProducts();

        // Assert
        assertEquals(1, result.size());
        assertEquals("Laptop", result.get(0).getName());
        assertEquals(
                new BigDecimal("15000000"),
                result.get(0).getPrice()
        );
        assertEquals("Gaming laptop", result.get(0).getDescription());
        assertEquals(10, result.get(0).getStock());

        verify(productRepository).findAll();
    }

    @Test
    void getProductById_shouldReturnProduct_whenIdExists() {
        // Arrange
        Long productId = 1L;

        Product product = new Product(
                "Laptop",
                new BigDecimal("15000000"),
                "Gaming laptop",
                10
        );

        // Giả lập repository tìm thấy sản phẩm
        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        // Act
        ProductResponse result =
                productService.getProductById(productId);

        // Assert
        assertEquals("Laptop", result.getName());
        assertEquals(
                new BigDecimal("15000000"),
                result.getPrice()
        );
        assertEquals("Gaming laptop", result.getDescription());
        assertEquals(10, result.getStock());

        verify(productRepository).findById(productId);
    }

    @Test
    void getProductById_shouldThrowException_whenIdNotExists() {
        // Arrange
        Long productId = 999L;

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getProductById(productId)
        );

        assertEquals(
                "Product not found with id: " + productId,
                exception.getMessage()
        );

        verify(productRepository).findById(productId);
    }
   
    @Test
    void createProduct_shouldSaveAndReturnProductResponse() {
        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("Keyboard");
        request.setPrice(new BigDecimal("1200000"));
        request.setDescription("Mechanical keyboard");
        request.setStock(20);
        request.setCategoryId(1L);

        Category category = new Category("Accessories", "Computer accessories");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Product savedProduct = new Product(
                "Keyboard",
                new BigDecimal("1200000"),
                "Mechanical keyboard",
                20
        );

        when(productRepository.save(
                org.mockito.ArgumentMatchers.any(Product.class)
        )).thenReturn(savedProduct);

        // Act
        ProductResponse result = productService.createProduct(request);

        // Assert
        assertEquals("Keyboard", result.getName());
        assertEquals(new BigDecimal("1200000"), result.getPrice());
        assertEquals("Mechanical keyboard", result.getDescription());
        assertEquals(20, result.getStock());

        verify(productRepository).save(
                org.mockito.ArgumentMatchers.any(Product.class)
        );
    }
    
    @Test
    void updateProduct_shouldUpdateAndReturnProduct_whenIdExists() {
        // Arrange
        Long productId = 1L;

        Product existingProduct = new Product(
                "Old Keyboard",
                new BigDecimal("800000"),
                "Old description",
                5
        );

        ProductRequest request = new ProductRequest();
        request.setName("New Keyboard");
        request.setPrice(new BigDecimal("1200000"));
        request.setDescription("New description");
        request.setStock(20);
        request.setCategoryId(1L);

        Category category = new Category("Accessories", "Computer accessories");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(existingProduct));

        when(productRepository.save(existingProduct))
                .thenReturn(existingProduct);

        // Act
        ProductResponse result =
                productService.updateProduct(productId, request);

        // Assert
        assertEquals("New Keyboard", result.getName());
        assertEquals(new BigDecimal("1200000"), result.getPrice());
        assertEquals("New description", result.getDescription());
        assertEquals(20, result.getStock());

        verify(productRepository).findById(productId);
        verify(productRepository).save(existingProduct);
    }

    @Test
    void updateProduct_shouldThrowException_whenIdNotExists() {
        // Arrange
        Long productId = 999L;

        ProductRequest request = new ProductRequest();
        request.setName("Keyboard");
        request.setPrice(new BigDecimal("1200000"));
        request.setDescription("Mechanical keyboard");
        request.setStock(20);

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.updateProduct(productId, request)
        );

        assertEquals(
                "Product not found with id: " + productId,
                exception.getMessage()
        );

        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(
                org.mockito.ArgumentMatchers.any(Product.class)
        );
    }

    @Test
    void deleteProduct_shouldCallRepositoryDeleteById() {
        // Arrange
        Long productId = 1L;
        when(productRepository.existsById(productId)).thenReturn(true);

        // Act
        productService.deleteProduct(productId);

        // Assert
        verify(productRepository).deleteById(productId);
    }

}
