package com.auth_service; 

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;

import com.auth_service.config.GlobalExceptionHandler;
import com.auth_service.dto.ApiResponse;

class GlobalExceptionHandlerTest {

    // Instantiate directly, no @Autowired or @WebMvcTest needed!
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleRuntime_ShouldReturn400() {
        RuntimeException ex = new RuntimeException("Custom business logic error");
        
        ResponseEntity<ApiResponse<String>> response = handler.handleRuntime(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Custom business logic error", response.getBody().getMessage());
    }

    @Test
    void handleMissingHeader_ShouldReturn400() {
        MethodParameter mockParam = mock(MethodParameter.class);
        MissingRequestHeaderException ex = new MissingRequestHeaderException("X-User-Id", mockParam);
        
        ResponseEntity<ApiResponse<String>> response = handler.handleMissingHeader(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Missing required header: X-User-Id", response.getBody().getMessage());
    }

    @Test
    void handleGeneral_ShouldReturn500() {
        Exception ex = new Exception("A massive database failure occurred");
        
        ResponseEntity<ApiResponse<String>> response = handler.handleGeneral(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Something went wrong", response.getBody().getMessage()); // Should mask the real error
    }

    @Test
    void handleValidation_ShouldReturn400WithFormattedErrors() {
        // Arrange: Mock the validation exception and binding result
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        FieldError fieldError = new FieldError("loginRequest", "email", "must be a well-formed email address");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        // Act
        ResponseEntity<ApiResponse<String>> response = handler.handleValidation(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("email: must be a well-formed email address", response.getBody().getMessage());
    }
}