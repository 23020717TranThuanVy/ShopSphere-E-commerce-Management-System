package com.shopsphere.controller;

import com.shopsphere.dto.ProductResponse;
import com.shopsphere.service.ProductService;
import com.shopsphere.exception.ResourceNotFoundException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void getAllProducts_shouldReturnProductList() throws Exception {
        // Arrange
        ProductResponse product = new ProductResponse(
                1L,
                "Laptop",
                new BigDecimal("15000000"),
                "Gaming laptop",
                10,
                1L,
                "Electronics"
        );

        when(productService.getAllProducts())
                .thenReturn(List.of(product));

        // Act & Assert
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[0].price").value(15000000))
                .andExpect(jsonPath("$[0].description").value("Gaming laptop"))
                .andExpect(jsonPath("$[0].stock").value(10));
    }
    
    @Test
    void getProductById_shouldReturnProduct() throws Exception {
        // Arrange
        ProductResponse product = new ProductResponse(
                1L,
                "Laptop",
                new BigDecimal("15000000"),
                "Gaming laptop",
                10,
                1L,
                "Electronics"
        );

        when(productService.getProductById(1L))
                .thenReturn(product);

        // Act & Assert
        mockMvc.perform(get("/api/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(15000000))
                .andExpect(jsonPath("$.description").value("Gaming laptop"))
                .andExpect(jsonPath("$.stock").value(10));
    }

    @Test
    void createProduct_shouldReturnCreatedProduct() throws Exception {
        // Arrange
        ProductResponse product = new ProductResponse(
                1L,
                "Keyboard",
                new BigDecimal("1200000"),
                "Mechanical keyboard",
                20,
                1L,
                "Accessories"
        );

        when(productService.createProduct(any(
                com.shopsphere.dto.ProductRequest.class
        ))).thenReturn(product);

        String requestJson = """
                {
                    "name": "Keyboard",
                    "price": 1200000,
                    "description": "Mechanical keyboard",
                    "stock": 20,
                    "categoryId": 1
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Keyboard"))
                .andExpect(jsonPath("$.price").value(1200000))
                .andExpect(jsonPath("$.description")
                        .value("Mechanical keyboard"))
                .andExpect(jsonPath("$.stock").value(20));

        verify(productService).createProduct(argThat(request ->
                request.getName().equals("Keyboard")
                        && request.getPrice().compareTo(
                                new BigDecimal("1200000")) == 0
                        && request.getDescription()
                                .equals("Mechanical keyboard")
                        && request.getStock().equals(20)
                        && request.getCategoryId().equals(1L)
        ));
    }
    
        @Test
        void createProduct_shouldReturnBadRequest_whenRequestIsInvalid()
                throws Exception {

        String requestJson = """
                {
                        "name": "",
                        "price": 0,
                        "description": "Invalid product",
                        "stock": -1
                }
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
        }
        
        @Test
        void updateProduct_shouldReturnUpdatedProduct() throws Exception {
        // Arrange
        ProductResponse product = new ProductResponse(
                1L,
                "Updated Laptop",
                new BigDecimal("18000000"),
                "Updated gaming laptop",
                15,
                1L,
                "Electronics"
        );

        when(productService.updateProduct(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any(
                        com.shopsphere.dto.ProductRequest.class
                )
        )).thenReturn(product);

        String requestJson = """
                {
                        "name": "Updated Laptop",
                        "price": 18000000,
                        "description": "Updated gaming laptop",
                        "stock": 15,
                        "categoryId": 1
                }
                """;

        // Act & Assert
        mockMvc.perform(put("/api/products/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Laptop"))
                .andExpect(jsonPath("$.price").value(18000000))
                .andExpect(jsonPath("$.description")
                        .value("Updated gaming laptop"))
                .andExpect(jsonPath("$.stock").value(15));
        }
        
        @Test
        void updateProduct_shouldReturnBadRequest_whenRequestIsInvalid()
                throws Exception {

        String requestJson = """
                {
                        "name": "",
                        "price": 0,
                        "description": "Invalid product",
                        "stock": -1
                }
                """;

        mockMvc.perform(put("/api/products/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
        }

        @Test
        void deleteProduct_shouldReturnNoContent() throws Exception {
        // Arrange
        Long productId = 1L;

        // Act & Assert
        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/products/{id}", productId)
        )
                .andExpect(status().isNoContent());

        // Xác nhận Controller gọi Service với đúng ID
        verify(productService).deleteProduct(productId);
        }

        @Test
        void getProductById_shouldReturnNotFound_whenIdDoesNotExist()
                throws Exception {

        // Arrange
        Long productId = 999L;

        when(productService.getProductById(productId))
                .thenThrow(new ResourceNotFoundException(
                        "Product not found with id: " + productId
                ));

        // Act & Assert                       
        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Product not found with id: " + productId));

        }

}
