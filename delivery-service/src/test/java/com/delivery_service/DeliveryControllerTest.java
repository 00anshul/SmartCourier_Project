package com.delivery_service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.delivery_service.controller.DeliveryController;
import com.delivery_service.dto.CreateDeliveryRequest;
import com.delivery_service.dto.SchedulePickupRequest;
import com.delivery_service.entity.Delivery;
import com.delivery_service.entity.DeliveryStatus;
import com.delivery_service.entity.Pickup;
import com.delivery_service.entity.ServiceType;
import com.delivery_service.service.DeliveryService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(DeliveryController.class)
@AutoConfigureMockMvc(addFilters = false) // Bypass Security filters
class DeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DeliveryService deliveryService;

 
     @MockitoBean
     private com.delivery_service.security.JwtUtil jwtUtil;

    private Delivery testDelivery;
    private Pickup testPickup;
    private Long customerId = 100L;
    private Long deliveryId = 1L;

    @BeforeEach
    void setUp() {
        testDelivery = new Delivery();
        testDelivery.setId(deliveryId);
        testDelivery.setTrackingNumber("SC-20260325-ABC12345");
        testDelivery.setCustomerId(customerId);
        testDelivery.setStatus(DeliveryStatus.valueOf("BOOKED")); // Assuming enum exists
        testDelivery.setServiceType(ServiceType.valueOf("DOMESTIC"));
        testDelivery.setTotalCharge(new BigDecimal("150.00"));

        testPickup = new Pickup();
        testPickup.setId(1L);
        testPickup.setDelivery(testDelivery);
        testPickup.setScheduledDate(LocalDate.now().plusDays(1));
        testPickup.setSlot("MORNING");
        testPickup.setStatus("PENDING");
    }

    // ── 1. Create Delivery ────────────────────────────────────────────────

    @Test
    void createDelivery_Success_ShouldReturn201() throws Exception {
        // 1. Create the request
        CreateDeliveryRequest request = new CreateDeliveryRequest();
        
        // 2. FILL OUT THE DUMMY DATA SO @Valid PASSES
        request.setServiceType(ServiceType.valueOf("DOMESTIC"));
        request.setNotes("Handle with care dummy test");

        // Populate Package Details
        com.delivery_service.dto.PackageRequest packageDetails = new com.delivery_service.dto.PackageRequest();
        packageDetails.setWeightKg(new BigDecimal("5.00"));
        packageDetails.setLengthCm(new BigDecimal("10.00"));
        packageDetails.setWidthCm(new BigDecimal("10.00"));
        packageDetails.setHeightCm(new BigDecimal("10.00"));
        request.setPackageDetails(packageDetails);

        // Populate Sender Address
        com.delivery_service.dto.AddressRequest sender = new com.delivery_service.dto.AddressRequest();
        sender.setFullName("John Sender");
        sender.setPhone("9999999999");
        sender.setStreet("123 Sender St");
        sender.setCity("Sender City");
        sender.setState("Sender State");
        sender.setPincode("123456");
        sender.setCountry("India");
        request.setSenderAddress(sender);

        // Populate Receiver Address
        com.delivery_service.dto.AddressRequest receiver = new com.delivery_service.dto.AddressRequest();
        receiver.setFullName("Jane Receiver");
        receiver.setPhone("8888888888");
        receiver.setStreet("456 Receiver St");
        receiver.setCity("Receiver City");
        receiver.setState("Receiver State");
        receiver.setPincode("654321");
        receiver.setCountry("India");
        request.setReceiverAddress(receiver);

        // 3. Mock the service call
        when(deliveryService.createDelivery(any(CreateDeliveryRequest.class), eq(customerId)))
                .thenReturn(testDelivery);

        // 4. Perform the mock HTTP request
        mockMvc.perform(post("/deliveries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("userId", customerId)) // Simulates the attribute set by your JWT filter
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Delivery created successfully"))
                .andExpect(jsonPath("$.data.trackingNumber").value("SC-20260325-ABC12345"));

        verify(deliveryService).createDelivery(any(CreateDeliveryRequest.class), eq(customerId));
    }
    // ── 2. Get My Deliveries (Pagination) ─────────────────────────────────

    @Test
    void getMyDeliveries_Success_ShouldReturn200AndPage() throws Exception {
        Page<Delivery> deliveryPage = new PageImpl<>(List.of(testDelivery));
        when(deliveryService.getMyDeliveries(eq(customerId), any(Pageable.class)))
                .thenReturn(deliveryPage);

        mockMvc.perform(get("/deliveries/my")
                .param("page", "0")
                .param("size", "20")
                .requestAttr("userId", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].trackingNumber").value("SC-20260325-ABC12345"));

        verify(deliveryService).getMyDeliveries(eq(customerId), any(Pageable.class));
    }

    // ── 3. Get Delivery By ID & Tracking Number ───────────────────────────

    @Test
    void getDeliveryById_Success_ShouldReturn200() throws Exception {
        when(deliveryService.getDeliveryById(deliveryId)).thenReturn(testDelivery);

        mockMvc.perform(get("/deliveries/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(deliveryId));
    }

    @Test
    void getByTrackingNumber_Success_ShouldReturn200() throws Exception {
        String trackingNo = "SC-20260325-ABC12345";
        when(deliveryService.getDeliveryByTrackingNumber(trackingNo)).thenReturn(testDelivery);

        mockMvc.perform(get("/deliveries/track/" + trackingNo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.trackingNumber").value(trackingNo));
    }

    // ── 4. Schedule Pickup ────────────────────────────────────────────────

    @Test
    void schedulePickup_Success_ShouldReturn201() throws Exception {
        SchedulePickupRequest request = new SchedulePickupRequest();
        request.setScheduledDate(LocalDate.now().plusDays(1));
        request.setSlot("MORNING");

        when(deliveryService.schedulePickup(eq(deliveryId), any(SchedulePickupRequest.class), eq(customerId)))
                .thenReturn(testPickup);

        mockMvc.perform(post("/deliveries/1/pickup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("userId", customerId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Pickup scheduled successfully"))
                .andExpect(jsonPath("$.data.slot").value("MORNING"));
    }

    // ── 5. Update Status ──────────────────────────────────────────────────

    @Test
    void updateStatus_Success_ShouldReturn200() throws Exception {
        String newStatus = "IN_TRANSIT";
        testDelivery.setStatus(DeliveryStatus.valueOf(newStatus));

        when(deliveryService.updateStatus(deliveryId, newStatus)).thenReturn(testDelivery);

        mockMvc.perform(put("/deliveries/1/status")
                .param("status", newStatus))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value(newStatus));
    }

    // ── 6. Get Quote ──────────────────────────────────────────────────────

    @Test
    void getQuote_Success_ShouldReturn200() throws Exception {
        BigDecimal expectedQuote = new BigDecimal("120.00");
        when(deliveryService.getQuote(anyString(), any(BigDecimal.class))).thenReturn(expectedQuote);

        mockMvc.perform(get("/deliveries/quote")
                .param("serviceType", "DOMESTIC")
                .param("weightKg", "5.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(120.00));
    }

    // ── 7. Get All Deliveries (Admin) ─────────────────────────────────────

    @Test
    void getAllDeliveries_Success_ShouldReturn200() throws Exception {
        Page<Delivery> deliveryPage = new PageImpl<>(List.of(testDelivery));
        when(deliveryService.getAllDeliveries(any(Pageable.class))).thenReturn(deliveryPage);

        mockMvc.perform(get("/deliveries")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(deliveryId));
    }
}