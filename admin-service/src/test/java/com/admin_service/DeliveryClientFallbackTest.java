package com.admin_service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.admin_service.config.DeliveryClientFallback;

class DeliveryClientFallbackTest {

    private DeliveryClientFallback fallback;

    @BeforeEach
    void setUp() {
        fallback = new DeliveryClientFallback();
    }

    @Test
    void getDeliveryById_ShouldReturnGracefulErrorMap() {
        Map<String, Object> response = fallback.getDeliveryById(99L);
        
        assertFalse((Boolean) response.get("success"));
        assertTrue(((String) response.get("message")).contains("unavailable"));
        assertNull(response.get("data"));
    }

    @Test
    void updateDeliveryStatus_ShouldReturnGracefulErrorMap() {
        Map<String, Object> response = fallback.updateDeliveryStatus(1L, "DELIVERED");
        
        assertFalse((Boolean) response.get("success"));
        assertTrue(((String) response.get("message")).contains("unavailable"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAllDeliveries_ShouldReturnEmptyContentListToPreventNullPointers() {
        Map<String, Object> response = fallback.getAllDeliveries(0, 20);
        
        assertFalse((Boolean) response.get("success"));
        
        // Extract the nested data map that the Admin dashboard expects
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        assertNotNull(data);
        
        // Ensure content is an empty list, not null
        List<?> content = (List<?>) data.get("content");
        assertTrue(content.isEmpty());
        assertEquals(0, data.get("totalElements"));
    }
}