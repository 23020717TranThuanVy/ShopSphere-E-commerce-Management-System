package com.shopsphere.exception;

import com.shopsphere.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleResourceNotFound_shouldReturn404() {
        // Arrange
        String errorMessage = "Product not found with id: 999";
        ResourceNotFoundException exception =
                new ResourceNotFoundException(errorMessage);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/products/999");

        // Act
        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleResourceNotFound(exception, request);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());

        ErrorResponse body = response.getBody();

        assertEquals(404, body.getStatus());
        assertEquals("Not Found", body.getError());
        assertEquals(errorMessage, body.getMessage());
        assertEquals("/api/products/999", body.getPath());
        assertNotNull(body.getTimestamp());
    }

    @Test
    void handleValidationErrors_shouldReturn400WithFieldErrors() {
        // Arrange
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "productRequest");

        bindingResult.addError(
                new FieldError(
                        "productRequest",
                        "name",
                        "",
                        false,
                        null,
                        null,
                        "Product name must not be blank"
                )
        );

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(null, bindingResult);

        // Act
        ResponseEntity<Map<String, Object>> response =
                exceptionHandler.handleValidationErrors(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> body = response.getBody();

        assertEquals(400, body.get("status"));
        assertEquals("Bad Request", body.get("error"));
        assertEquals("Validation failed", body.get("message"));
        assertNotNull(body.get("timestamp"));

        @SuppressWarnings("unchecked")
        Map<String, String> fieldErrors =
                (Map<String, String>) body.get("fieldErrors");

        assertNotNull(fieldErrors);
        assertEquals(
                "Product name must not be blank",
                fieldErrors.get("name")
        );
    }
}
