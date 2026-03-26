package com.admin_service.config;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class DeliveryClientFallback implements DeliveryClient {

    @Override
    public Map<String, Object> getDeliveryById(Long id) {
        return createErrorMap("Delivery service is currently unavailable. Cannot fetch delivery " + id);
    }

    @Override
    public Map<String, Object> updateDeliveryStatus(Long id, String status) {
        return createErrorMap("Delivery service unavailable. Cannot update status to " + status);
    }

    @Override
    public Map<String, Object> getAllDeliveries(int page, int size) {
        Map<String, Object> response = createErrorMap("Delivery service unavailable.");
        // Return an empty list for 'content' so the Admin dashboard doesn't crash trying to iterate over it
        Map<String, Object> data = new HashMap<>();
        data.put("content", java.util.List.of());
        data.put("totalElements", 0);
        response.put("data", data);
        return response;
    }

    private Map<String, Object> createErrorMap(String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("success", false);
        map.put("message", message);
        map.put("data", null);
        return map;
    }
}