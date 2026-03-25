package com.tracking_service.controller;

import com.tracking_service.dto.ApiResponse;
import com.tracking_service.dto.DeliveryProofRequest;
import com.tracking_service.dto.TrackingEventRequest;
import com.tracking_service.entity.DeliveryProof;
import com.tracking_service.entity.Document;
import com.tracking_service.entity.TrackingEvent;
import com.tracking_service.service.TrackingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/tracking")
@CrossOrigin("*")
@SecurityRequirement(name = "bearerAuth")
public class TrackingController {

    @Autowired
    private TrackingService trackingService;

    @PostMapping("/events")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TrackingEvent>> createEvent(
            @Valid @RequestBody TrackingEventRequest request,
            HttpServletRequest httpRequest) {

        Long adminId = (Long) httpRequest.getAttribute("userId");
        TrackingEvent event = trackingService
                .createTrackingEvent(request, adminId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true,
                        "Tracking event created", event));
    }

    @GetMapping("/{trackingNumber}")
    public ResponseEntity<ApiResponse<List<TrackingEvent>>> getByTrackingNumber(
            @PathVariable String trackingNumber) {

        List<TrackingEvent> events = trackingService
                .getEventsByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Tracking events found", events));
    }

    @GetMapping("/delivery/{deliveryId}")
    public ResponseEntity<ApiResponse<List<TrackingEvent>>> getByDeliveryId(
            @PathVariable Long deliveryId) {

        List<TrackingEvent> events = trackingService
                .getEventsByDeliveryId(deliveryId);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Tracking events found", events));
    }

    @PostMapping(value = "/documents/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Document>> uploadDocument(
            @RequestParam Long deliveryId,
            @RequestParam MultipartFile file,
            HttpServletRequest httpRequest) throws IOException {

        Long userId = (Long) httpRequest.getAttribute("userId");
        Document document = trackingService
                .uploadDocument(deliveryId, file, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true,
                        "Document uploaded successfully", document));
    }

    @GetMapping("/documents/{deliveryId}")
    public ResponseEntity<ApiResponse<List<Document>>> getDocuments(
            @PathVariable Long deliveryId) {

        List<Document> documents = trackingService
                .getDocumentsByDeliveryId(deliveryId);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Documents found", documents));
    }

    @PostMapping("/proof")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DeliveryProof>> addProof(
            @Valid @RequestBody DeliveryProofRequest request,
            HttpServletRequest httpRequest) {

        Long adminId = (Long) httpRequest.getAttribute("userId");
        DeliveryProof proof = trackingService
                .addDeliveryProof(request, adminId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true,
                        "Delivery proof added", proof));
    }

    @GetMapping("/proof/{deliveryId}")
    public ResponseEntity<ApiResponse<DeliveryProof>> getProof(
            @PathVariable Long deliveryId) {

        DeliveryProof proof = trackingService
                .getProofByDeliveryId(deliveryId);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Proof found", proof));
    }
}