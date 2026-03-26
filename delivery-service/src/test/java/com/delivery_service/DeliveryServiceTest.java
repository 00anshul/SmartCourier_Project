package com.delivery_service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.delivery_service.config.RabbitMQConfig;
import com.delivery_service.dto.CreateDeliveryRequest;
import com.delivery_service.dto.SchedulePickupRequest;
import com.delivery_service.entity.Delivery;
import com.delivery_service.entity.DeliveryStatus;
import com.delivery_service.entity.Pickup;
import com.delivery_service.entity.ServiceRate;
import com.delivery_service.entity.ServiceType;
import com.delivery_service.repository.AddressRepository;
import com.delivery_service.repository.DeliveryRepository;
import com.delivery_service.repository.PackageRepository;
import com.delivery_service.repository.PickupRepository;
import com.delivery_service.repository.ServiceRateRepository;
import com.delivery_service.service.DeliveryService;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private PackageRepository packageRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private PickupRepository pickupRepository;

    @Mock
    private ServiceRateRepository serviceRateRepository;

    @InjectMocks
    private DeliveryService deliveryService;

    private ServiceRate mockRate;
    private Delivery mockDelivery;
    private Long customerId = 100L;
    private Long deliveryId = 1L;

    @BeforeEach
    void setUp() {
        // Set up a mock service rate for charge calculation tests
        // Formula: Base(50.00) + (Weight * RatePerKg(10.00)) + Surcharge(20.00)
        mockRate = new ServiceRate();
        mockRate.setServiceType(ServiceType.valueOf("DOMESTIC")); // Assuming DOMESTIC exists
        mockRate.setBaseRate(new BigDecimal("50.00"));
        mockRate.setRatePerKg(new BigDecimal("10.00"));
        mockRate.setSurcharge(new BigDecimal("20.00"));

        mockDelivery = new Delivery();
        mockDelivery.setId(deliveryId);
        mockDelivery.setCustomerId(customerId);
        mockDelivery.setTrackingNumber("SC-20260325-ABC12345");
        mockDelivery.setStatus(DeliveryStatus.BOOKED);
    }

    // ── 1. Create Delivery & Charge Calculation ───────────────────────────

    @Test
    void createDelivery_Success_ShouldCalculateChargeAndSaveData() {
        // Arrange
        CreateDeliveryRequest mockRequest = mock(CreateDeliveryRequest.class);
        com.delivery_service.dto.AddressRequest mockAddress = mock(com.delivery_service.dto.AddressRequest.class);
        
        // Mocking the nested PackageDetails DTO to return a 5kg weight
        com.delivery_service.dto.PackageRequest mockPackageDetails = mock(com.delivery_service.dto.PackageRequest.class);
        when(mockRequest.getServiceType()).thenReturn(ServiceType.valueOf("DOMESTIC"));
        when(mockRequest.getPackageDetails()).thenReturn(mockPackageDetails);
        when(mockPackageDetails.getWeightKg()).thenReturn(new BigDecimal("5.00"));
        
        when(mockRequest.getSenderAddress()).thenReturn(mockAddress);
        when(mockRequest.getReceiverAddress()).thenReturn(mockAddress);

        when(serviceRateRepository.findByServiceType(any())).thenReturn(Optional.of(mockRate));
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Delivery result = deliveryService.createDelivery(mockRequest, customerId);

        // Assert
        assertNotNull(result);
        assertEquals(DeliveryStatus.BOOKED, result.getStatus());
        assertTrue(result.getTrackingNumber().startsWith("SC-"));
        
        // Charge should be: 50 + (5 * 10) + 20 = 120.00
        assertEquals(0, new BigDecimal("120.00").compareTo(result.getTotalCharge()), "Charge should be 120");
        // Verify relationships were saved
        verify(packageRepository, times(1)).save(any());
        verify(addressRepository, times(2)).save(any()); // Sender and Receiver
    }

    @Test
    void getQuote_Success_ShouldReturnCorrectMath() {
        // Arrange
        when(serviceRateRepository.findByServiceType(any())).thenReturn(Optional.of(mockRate));
        BigDecimal weight = new BigDecimal("2.5"); // 2.5 kg

        // Act
        BigDecimal quote = deliveryService.getQuote("DOMESTIC", weight);

        // Assert: 50 + (2.5 * 10) + 20 = 95.00
        assertEquals(0, new BigDecimal("95.00").compareTo(quote), "Quote should be 95");
    }

    // ── 2. Status Updates & RabbitMQ Publishing ───────────────────────────

    @Test
    void updateStatus_Success_ShouldUpdateAndPublishToRabbitMQ() {
        // Arrange
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(mockDelivery));
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(mockDelivery);

        // Act
        Delivery result = deliveryService.updateStatus(deliveryId, "IN_TRANSIT");

        // Assert
        assertEquals(DeliveryStatus.valueOf("IN_TRANSIT"), result.getStatus());

        // Verify RabbitMQ publishing format (deliveryId:trackingNumber:status)
        String expectedMessage = deliveryId + ":" + mockDelivery.getTrackingNumber() + ":IN_TRANSIT";
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.DELIVERY_STATUS_EXCHANGE),
                eq(RabbitMQConfig.DELIVERY_STATUS_KEY),
                eq(expectedMessage)
        );
    }

    // ── 3. Pickup Scheduling & Validation Logic ───────────────────────────

    @Test
    void schedulePickup_Success_ShouldSavePickup() {
        // Arrange
        SchedulePickupRequest request = new SchedulePickupRequest();
        request.setScheduledDate(LocalDate.now().plusDays(1)); // Future date
        request.setSlot("MORNING");

        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(mockDelivery));
        when(pickupRepository.findByDeliveryId(deliveryId)).thenReturn(Optional.empty());
        when(pickupRepository.save(any(Pickup.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Pickup result = deliveryService.schedulePickup(deliveryId, request, customerId);

        // Assert
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        assertEquals("MORNING", result.getSlot());
    }

    @Test
    void schedulePickup_WrongCustomer_ShouldThrowException() {
        // Arrange
        SchedulePickupRequest request = new SchedulePickupRequest();
        Long wrongCustomerId = 999L;
        
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(mockDelivery));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            deliveryService.schedulePickup(deliveryId, request, wrongCustomerId);
        });
        assertTrue(exception.getMessage().contains("not authorized"));
    }

    @Test
    void schedulePickup_DateInPast_ShouldThrowException() {
        // Arrange
        SchedulePickupRequest request = new SchedulePickupRequest();
        request.setScheduledDate(LocalDate.now().minusDays(1)); // Past date
        
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(mockDelivery));
        when(pickupRepository.findByDeliveryId(deliveryId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            deliveryService.schedulePickup(deliveryId, request, customerId);
        });
        assertTrue(exception.getMessage().contains("future date"));
    }

    @Test
    void schedulePickup_AlreadyExists_ShouldThrowException() {
        // Arrange
        SchedulePickupRequest request = new SchedulePickupRequest();
        
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(mockDelivery));
        when(pickupRepository.findByDeliveryId(deliveryId)).thenReturn(Optional.of(new Pickup()));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            deliveryService.schedulePickup(deliveryId, request, customerId);
        });
        assertTrue(exception.getMessage().contains("already scheduled"));
    }
}