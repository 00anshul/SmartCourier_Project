package com.tracking_service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.tracking_service.dto.DeliveryProofRequest;
import com.tracking_service.dto.TrackingEventRequest;
import com.tracking_service.entity.DeliveryProof;
import com.tracking_service.entity.Document;
import com.tracking_service.entity.TrackingEvent;
import com.tracking_service.repository.DeliveryProofRepository;
import com.tracking_service.repository.DocumentRepository;
import com.tracking_service.repository.TrackingEventRepository;
import com.tracking_service.service.TrackingService;

@ExtendWith(MockitoExtension.class)
class TrackingServiceTest {

    @Mock
    private TrackingEventRepository trackingEventRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DeliveryProofRepository deliveryProofRepository;

    @InjectMocks
    private TrackingService trackingService;

    private final Long deliveryId = 100L;
    private final String trackingNumber = "SC-20260326-ABC12345";
    private final Long adminId = 1L;

    @BeforeEach
    void setUp() {
        // General setup if needed
    }

    // ── 1. RabbitMQ Consumer Testing ─────────────────────────────────────

    @Test
    void consumeDeliveryStatusChange_ValidMessage_ShouldSaveEvent() {
        // Arrange
        String message = deliveryId + ":" + trackingNumber + ":IN_TRANSIT";
        
        // Use an ArgumentCaptor to intercept the entity before it's "saved"
        ArgumentCaptor<TrackingEvent> eventCaptor = ArgumentCaptor.forClass(TrackingEvent.class);

        // Act
        trackingService.consumeDeliveryStatusChange(message);

        // Assert
        verify(trackingEventRepository).save(eventCaptor.capture());
        TrackingEvent savedEvent = eventCaptor.getValue();
        
        assertEquals(deliveryId, savedEvent.getDeliveryId());
        assertEquals(trackingNumber, savedEvent.getTrackingNumber());
        assertEquals("IN_TRANSIT", savedEvent.getStatus());
        assertEquals("Status updated to IN_TRANSIT", savedEvent.getLocationDescription());
    }


    @Test
    void consumeDeliveryStatusChange_InvalidMessageFormat_ShouldThrowRejectException() {
        // Arrange - Missing the status part (only 2 parts instead of 3)
        String invalidMessage = deliveryId + ":" + trackingNumber;

        // Act & Assert
        Exception exception = assertThrows(AmqpRejectAndDontRequeueException.class, () -> {
            trackingService.consumeDeliveryStatusChange(invalidMessage);
        });
        
        // Verify the exception message contains our custom text
        assertTrue(exception.getMessage().contains("Invalid message format"));
        
        // Verify it never actually tried to save bad data to the database
        verify(trackingEventRepository, never()).save(any(TrackingEvent.class));
    }
    // ── 2. Manual Tracking Event Creation ────────────────────────────────

    @Test
    void createTrackingEvent_Success_ShouldReturnSavedEvent() {
        // Arrange
        TrackingEventRequest request = mock(TrackingEventRequest.class);
        when(request.getDeliveryId()).thenReturn(deliveryId);
        when(request.getTrackingNumber()).thenReturn(trackingNumber);
        when(request.getStatus()).thenReturn("OUT_FOR_DELIVERY");
        
        TrackingEvent mockSavedEvent = new TrackingEvent();
        mockSavedEvent.setStatus("OUT_FOR_DELIVERY");
        
        when(trackingEventRepository.save(any(TrackingEvent.class))).thenReturn(mockSavedEvent);

        // Act
        TrackingEvent result = trackingService.createTrackingEvent(request, adminId);

        // Assert
        assertNotNull(result);
        assertEquals("OUT_FOR_DELIVERY", result.getStatus());
        verify(trackingEventRepository).save(any(TrackingEvent.class));
    }

    // ── 3. Document Upload Validation Logic ──────────────────────────────

    @Test
    void uploadDocument_ExceedsCountLimit_ShouldThrowException() {
        // Arrange
        when(documentRepository.countByDeliveryId(deliveryId)).thenReturn(3L); // Max allowed is 3
        MultipartFile file = new MockMultipartFile("file", "invoice.pdf", "application/pdf", "dummy data".getBytes());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            trackingService.uploadDocument(deliveryId, file, adminId);
        });
        assertTrue(exception.getMessage().contains("Maximum 3 documents"));
    }

    @Test
    void uploadDocument_ExceedsSizeLimit_ShouldThrowException() {
        // Arrange
        when(documentRepository.countByDeliveryId(deliveryId)).thenReturn(1L);
        
        // Create a mock file that reports its size as 6MB (limit is 5MB)
        MultipartFile oversizedFile = mock(MultipartFile.class);
        when(oversizedFile.getSize()).thenReturn(6L * 1024 * 1024);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            trackingService.uploadDocument(deliveryId, oversizedFile, adminId);
        });
        assertTrue(exception.getMessage().contains("exceeds 5MB limit"));
    }

    @Test
    void uploadDocument_InvalidFileType_ShouldThrowException() throws IOException {
        // Arrange
        when(documentRepository.countByDeliveryId(deliveryId)).thenReturn(1L);
        
        // Mock a valid size, but an invalid extension (.exe instead of pdf/jpg/png)
        MultipartFile badExtensionFile = mock(MultipartFile.class);
        when(badExtensionFile.getSize()).thenReturn(1024L);
        when(badExtensionFile.getOriginalFilename()).thenReturn("virus.exe");

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            trackingService.uploadDocument(deliveryId, badExtensionFile, adminId);
        });
        assertTrue(exception.getMessage().contains("Only PDF, JPG and PNG"));
    }

    // ── 4. Delivery Proof Logic ──────────────────────────────────────────

    @Test
    void addDeliveryProof_Success_ShouldSaveProof() {
        // Arrange
        DeliveryProofRequest request = mock(DeliveryProofRequest.class);
        when(request.getDeliveryId()).thenReturn(deliveryId);
        when(request.getReceivedBy()).thenReturn("John Doe");

        when(deliveryProofRepository.findByDeliveryId(deliveryId)).thenReturn(Optional.empty());
        when(deliveryProofRepository.save(any(DeliveryProof.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        DeliveryProof result = trackingService.addDeliveryProof(request, adminId);

        // Assert
        assertNotNull(result);
        assertEquals(deliveryId, result.getDeliveryId());
        assertEquals("John Doe", result.getReceivedBy());
    }

    @Test
    void addDeliveryProof_AlreadyExists_ShouldThrowException() {
        // Arrange
        DeliveryProofRequest request = mock(DeliveryProofRequest.class);
        when(request.getDeliveryId()).thenReturn(deliveryId);
        
        when(deliveryProofRepository.findByDeliveryId(deliveryId)).thenReturn(Optional.of(new DeliveryProof()));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            trackingService.addDeliveryProof(request, adminId);
        });
        assertTrue(exception.getMessage().contains("already exists"));
    }
}