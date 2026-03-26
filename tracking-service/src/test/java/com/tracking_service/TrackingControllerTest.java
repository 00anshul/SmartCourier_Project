package com.tracking_service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracking_service.controller.TrackingController;
import com.tracking_service.dto.DeliveryProofRequest;
import com.tracking_service.dto.TrackingEventRequest;
import com.tracking_service.entity.DeliveryProof;
import com.tracking_service.entity.Document;
import com.tracking_service.entity.TrackingEvent;
import com.tracking_service.service.TrackingService;

@WebMvcTest(TrackingController.class)
@AutoConfigureMockMvc(addFilters = false) // Bypass Security filters
class TrackingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TrackingService trackingService;

    // Prevents ApplicationContext from crashing if JwtAuthFilter is scanned
    @MockitoBean
    private com.tracking_service.security.JwtUtil jwtUtil; // Adjust package if necessary

    private final Long adminId = 1L;
    private final Long customerId = 100L;
    private final Long deliveryId = 500L;
    private final String trackingNumber = "SC-20260326-ABC12345";

    private TrackingEvent mockEvent;
    private Document mockDocument;
    private DeliveryProof mockProof;

    @BeforeEach
    void setUp() {
        mockEvent = new TrackingEvent();
        mockEvent.setId(1L);
        mockEvent.setDeliveryId(deliveryId);
        mockEvent.setTrackingNumber(trackingNumber);
        mockEvent.setStatus("IN_TRANSIT");
        mockEvent.setLocationDescription("Arrived at Hub");

        mockDocument = new Document();
        mockDocument.setId(1L);
        mockDocument.setDeliveryId(deliveryId);
        mockDocument.setFileName("invoice.pdf");
        mockDocument.setFileType("PDF");

        mockProof = new DeliveryProof();
        mockProof.setId(1L);
        mockProof.setDeliveryId(deliveryId);
        mockProof.setReceivedBy("John Doe");
    }

    // ── 1. Create Event ──────────────────────────────────────────────────

    @Test
    void createEvent_Success_ShouldReturn201() throws Exception {
        // 1. Setup valid request (Update setters if your DTO field names differ)
        TrackingEventRequest request = new TrackingEventRequest();
        request.setDeliveryId(deliveryId);
        request.setTrackingNumber(trackingNumber);
        request.setStatus("IN_TRANSIT");
        request.setLocationDescription("Arrived at Hub");

        when(trackingService.createTrackingEvent(any(TrackingEventRequest.class), eq(adminId)))
                .thenReturn(mockEvent);

        mockMvc.perform(post("/tracking/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("userId", adminId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tracking event created"))
                .andExpect(jsonPath("$.data.status").value("IN_TRANSIT"));
    }

    // ── 2. Get Events by Tracking Number & Delivery ID ───────────────────

    @Test
    void getByTrackingNumber_Success_ShouldReturn200() throws Exception {
        when(trackingService.getEventsByTrackingNumber(trackingNumber)).thenReturn(List.of(mockEvent));

        mockMvc.perform(get("/tracking/" + trackingNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].trackingNumber").value(trackingNumber));
    }

    @Test
    void getByDeliveryId_Success_ShouldReturn200() throws Exception {
        when(trackingService.getEventsByDeliveryId(deliveryId)).thenReturn(List.of(mockEvent));

        mockMvc.perform(get("/tracking/delivery/" + deliveryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].deliveryId").value(deliveryId));
    }

    // ── 3. Upload Document (Multipart File Test) ─────────────────────────

    @Test
    void uploadDocument_Success_ShouldReturn201() throws Exception {
        // Create a mock file
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",               // The @RequestParam name
                "invoice.pdf",        // Original filename
                MediaType.APPLICATION_PDF_VALUE, 
                "dummy pdf content".getBytes()
        );

        when(trackingService.uploadDocument(eq(deliveryId), any(), eq(customerId)))
                .thenReturn(mockDocument);

        // Use multipart() instead of post() to simulate form-data
        mockMvc.perform(multipart("/tracking/documents/upload")
                .file(mockFile)
                .param("deliveryId", deliveryId.toString())
                .requestAttr("userId", customerId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Document uploaded successfully"))
                .andExpect(jsonPath("$.data.fileName").value("invoice.pdf"));
    }

    @Test
    void getDocuments_Success_ShouldReturn200() throws Exception {
        when(trackingService.getDocumentsByDeliveryId(deliveryId)).thenReturn(List.of(mockDocument));

        mockMvc.perform(get("/tracking/documents/" + deliveryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].fileName").value("invoice.pdf"));
    }

    // ── 4. Delivery Proof ────────────────────────────────────────────────

    @Test
    void addProof_Success_ShouldReturn201() throws Exception {
        // Setup valid request to pass @Valid
        DeliveryProofRequest request = new DeliveryProofRequest();
        request.setDeliveryId(deliveryId);
        request.setReceivedBy("John Doe");
        request.setRemarks("Left at front door");

        when(trackingService.addDeliveryProof(any(DeliveryProofRequest.class), eq(adminId)))
                .thenReturn(mockProof);

        mockMvc.perform(post("/tracking/proof")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("userId", adminId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Delivery proof added"))
                .andExpect(jsonPath("$.data.receivedBy").value("John Doe"));
    }

    @Test
    void getProof_Success_ShouldReturn200() throws Exception {
        when(trackingService.getProofByDeliveryId(deliveryId)).thenReturn(mockProof);

        mockMvc.perform(get("/tracking/proof/" + deliveryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.receivedBy").value("John Doe"));
    }
}