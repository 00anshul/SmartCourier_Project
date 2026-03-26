package com.admin_service.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;

// ADD THE FALLBACK HERE:
@FeignClient(name = "delivery-service", 
             url = "${feign.client.config.delivery-service.url}",
             fallback = DeliveryClientFallback.class)
public interface DeliveryClient {

    @GetMapping("/deliveries/{id}")
    Map<String, Object> getDeliveryById(@PathVariable Long id);

    @PutMapping("/deliveries/{id}/status")
    Map<String, Object> updateDeliveryStatus(@PathVariable Long id, @RequestParam String status);

    @GetMapping("/deliveries")
    Map<String, Object> getAllDeliveries(@RequestParam(defaultValue = "0") int page, 
                                         @RequestParam(defaultValue = "20") int size);
}