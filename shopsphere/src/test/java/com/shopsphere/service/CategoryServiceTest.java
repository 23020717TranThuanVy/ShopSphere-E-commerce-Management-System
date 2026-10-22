package com.shopsphere.service;

import com.shopsphere.dto.CategoryRequest;
import com.shopsphere.dto.CategoryResponse;
import com.shopsphere.entity.Category;
import com.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.repository.CategoryRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category(
                "Electronics",
                "Electronic devices"
        );
    }

    @Test
    void getAllCategories_returnsCategories() {
        when(categoryRepository.findAll())
                .thenReturn(List.of(category));

        List<CategoryResponse> result =
                categoryService.getAllCategories();

        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).getName());
    }

    @Test
    void getCategoryById_whenExists_returnsCategory() {
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        CategoryResponse result =
                categoryService.getCategoryById(1L);

        assertEquals("Electronics", result.getName());
    }

    @Test
    void getCategoryById_whenNotExists_throwsException() {
        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.getCategoryById(99L)
        );
    }

    @Test
    void createCategory_savesAndReturnsCategory() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Electronics");
        request.setDescription("Electronic devices");

        when(categoryRepository.save(any(Category.class)))
                .thenReturn(category);

        CategoryResponse result =
                categoryService.createCategory(request);

        assertEquals("Electronics", result.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_whenExists_updatesAndReturnsCategory() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Books");
        request.setDescription("Books and magazines");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse result =
                categoryService.updateCategory(1L, request);

        assertEquals("Books", result.getName());
        assertEquals("Books and magazines", result.getDescription());
    }

    @Test
    void deleteCategory_whenExists_deletesCategory() {
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        categoryService.deleteCategory(1L);

        verify(categoryRepository).delete(category);
    }
}
