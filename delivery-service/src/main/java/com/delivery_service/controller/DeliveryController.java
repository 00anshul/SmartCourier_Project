package com.delivery_service.controller;

import com.delivery_service.dto.ApiResponse;
import com.delivery_service.dto.CreateDeliveryRequest;
import com.delivery_service.dto.SchedulePickupRequest;
import com.delivery_service.entity.Delivery;
import com.delivery_service.entity.Pickup;
import com.delivery_service.service.DeliveryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/deliveries")
@CrossOrigin("*")
@SecurityRequirement(name = "bearerAuth")
public class DeliveryController {


    @Autowired
    private DeliveryService deliveryService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Delivery>> createDelivery(
            @Valid @RequestBody CreateDeliveryRequest request,
            HttpServletRequest httpRequest) {

        Long customerId = (Long) httpRequest.getAttribute("userId");
        Delivery delivery = deliveryService.createDelivery(request, customerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true,
                        "Delivery created successfully", delivery));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<Delivery>>> getMyDeliveries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {

        Long customerId = (Long) httpRequest.getAttribute("userId");
        Page<Delivery> deliveries = deliveryService.getMyDeliveries(
                customerId,
                PageRequest.of(page, size,
                        Sort.by("createdAt").descending()));
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Deliveries fetched", deliveries));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Delivery>> getDeliveryById(
            @PathVariable Long id) {

        Delivery delivery = deliveryService.getDeliveryById(id);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Delivery found", delivery));
    }

    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<ApiResponse<Delivery>> getByTrackingNumber(
            @PathVariable String trackingNumber) {

        Delivery delivery = deliveryService
                .getDeliveryByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Delivery found", delivery));
    }

    @PostMapping("/{id}/pickup")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Pickup>> schedulePickup(
            @PathVariable Long id,
            @Valid @RequestBody SchedulePickupRequest request,
            HttpServletRequest httpRequest) {

        Long customerId = (Long) httpRequest.getAttribute("userId");
        Pickup pickup = deliveryService.schedulePickup(id, request, customerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true,
                        "Pickup scheduled successfully", pickup));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Delivery>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        Delivery delivery = deliveryService.updateStatus(id, status);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Status updated", delivery));
    }

    @GetMapping("/quote")
    public ResponseEntity<ApiResponse<BigDecimal>> getQuote(
            @RequestParam String serviceType,
            @RequestParam BigDecimal weightKg) {

        BigDecimal quote = deliveryService.getQuote(serviceType, weightKg);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Quote calculated", quote));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<Delivery>>> getAllDeliveries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Delivery> deliveries = deliveryService.getAllDeliveries(
                PageRequest.of(page, size,
                        Sort.by("createdAt").descending()));
        return ResponseEntity.ok(
                new ApiResponse<>(true, "All deliveries fetched", deliveries));
    }
}