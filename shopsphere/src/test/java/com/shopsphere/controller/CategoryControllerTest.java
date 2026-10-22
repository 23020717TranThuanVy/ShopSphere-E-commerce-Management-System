package com.shopsphere.controller;

import com.shopsphere.dto.CategoryRequest;
import com.shopsphere.dto.CategoryResponse;
import com.shopsphere.exception.GlobalExceptionHandler;
import com.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.service.CategoryService;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void getAllCategories_returnsOk() throws Exception {
        when(categoryService.getAllCategories())
                .thenReturn(List.of(
                        new CategoryResponse(1L, "Electronics", "Devices")
                ));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Electronics"));
    }

    @Test
    void getCategoryById_returnsOk() throws Exception {
        when(categoryService.getCategoryById(1L))
                .thenReturn(new CategoryResponse(1L, "Electronics", "Devices"));

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void createCategory_returnsCreated() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Electronics");
        request.setDescription("Devices");

        when(categoryService.createCategory(any(CategoryRequest.class)))
                .thenReturn(new CategoryResponse(1L, "Electronics", "Devices"));

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void createCategory_withBlankName_returnsBadRequest() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("   ");
        request.setDescription("Devices");

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(categoryService);
    }

    @Test
    void updateCategory_returnsOk() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Books");
        request.setDescription("Reading");

        when(categoryService.updateCategory(eq(1L), any(CategoryRequest.class)))
                .thenReturn(new CategoryResponse(1L, "Books", "Reading"));

        mockMvc.perform(put("/api/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Books"));
    }

    @Test
    void deleteCategory_returnsNoContent() throws Exception {
        doNothing().when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());

        verify(categoryService).deleteCategory(1L);
    }

    @Test
    void getCategoryById_whenNotFound_returnsNotFound() throws Exception {
        when(categoryService.getCategoryById(99L))
                .thenThrow(new ResourceNotFoundException(
                        "Category not found with id: 99"
                ));

        mockMvc.perform(get("/api/categories/99"))
                .andExpect(status().isNotFound());
    }
}
