package com.admin_service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.admin_service.config.DeliveryClient;
import com.admin_service.dto.ResolveExceptionRequest;
import com.admin_service.dto.ReportRequest;
import com.admin_service.entity.ExceptionLog;
import com.admin_service.entity.Report;
import com.admin_service.repository.ExceptionLogRepository;
import com.admin_service.repository.HubRepository;
import com.admin_service.repository.ReportRepository;
import com.admin_service.service.AdminService;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private HubRepository hubRepository;

    @Mock
    private ExceptionLogRepository exceptionLogRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private DeliveryClient deliveryClient;

    @InjectMocks
    private AdminService adminService;

    private Long adminId = 999L;
    private Long deliveryId = 1L;

    @BeforeEach
    void setUp() {
        // Any global setup can go here
    }

    // ── 1. Resolve Exception (Testing the Feign unwrapping bug fix!) ──────

    @Test
    void resolveException_Retry_ShouldUnwrapFeignResponseAndSaveLog() {
        // Arrange
        ResolveExceptionRequest mockRequest = mock(ResolveExceptionRequest.class);
        when(mockRequest.getDeliveryId()).thenReturn(deliveryId);
        when(mockRequest.getAction()).thenReturn("RETRY");
        when(mockRequest.getNote()).thenReturn("Customer requested retry");

        // Simulate the Feign client returning the nested ApiResponse as a Map
        Map<String, Object> deliveryData = Map.of("status", "FAILED");
        Map<String, Object> feignResponse = Map.of("data", deliveryData, "success", true);

        when(deliveryClient.getDeliveryById(deliveryId)).thenReturn(feignResponse);
        when(exceptionLogRepository.save(any(ExceptionLog.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        ExceptionLog result = adminService.resolveException(mockRequest, adminId);

        // Assert
        assertNotNull(result);
        assertEquals(deliveryId, result.getDeliveryId());
        assertEquals("RETRY", result.getAction());
        assertEquals("FAILED", result.getPreviousStatus()); // Proves the unwrapping works!
        assertEquals("OUT_FOR_DELIVERY", result.getNewStatus()); // Proves the switch statement works!
        assertEquals("Customer requested retry", result.getNote());
        assertEquals(adminId, result.getResolvedBy());

        // Verify the external update call was made with the new status
        verify(deliveryClient).updateDeliveryStatus(deliveryId, "OUT_FOR_DELIVERY");
        verify(exceptionLogRepository).save(any(ExceptionLog.class));
    }

    @Test
    void resolveException_Refund_ShouldMapToRefundedStatus() {
        // Arrange
        ResolveExceptionRequest mockRequest = mock(ResolveExceptionRequest.class);
        when(mockRequest.getDeliveryId()).thenReturn(deliveryId);
        when(mockRequest.getAction()).thenReturn("REFUND");

        Map<String, Object> deliveryData = Map.of("status", "RETURNED");
        Map<String, Object> feignResponse = Map.of("data", deliveryData);

        when(deliveryClient.getDeliveryById(deliveryId)).thenReturn(feignResponse);
        when(exceptionLogRepository.save(any(ExceptionLog.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        ExceptionLog result = adminService.resolveException(mockRequest, adminId);

        // Assert
        assertEquals("REFUNDED", result.getNewStatus());
        verify(deliveryClient).updateDeliveryStatus(deliveryId, "REFUNDED");
    }

    @Test
    void resolveException_DeliveryNotFound_ShouldThrowException() {
        // Arrange
        ResolveExceptionRequest mockRequest = mock(ResolveExceptionRequest.class);
        when(mockRequest.getDeliveryId()).thenReturn(deliveryId);
        
        // Simulate Feign returning null
        when(deliveryClient.getDeliveryById(deliveryId)).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            adminService.resolveException(mockRequest, adminId);
        });
        assertEquals("Delivery not found", exception.getMessage());
    }

    @Test
    void resolveException_InvalidAction_ShouldThrowException() {
        // Arrange
        ResolveExceptionRequest mockRequest = mock(ResolveExceptionRequest.class);
        when(mockRequest.getDeliveryId()).thenReturn(deliveryId);
        when(mockRequest.getAction()).thenReturn("EXPLODE"); // Invalid action

        Map<String, Object> feignResponse = Map.of("data", Map.of("status", "FAILED"));
        when(deliveryClient.getDeliveryById(deliveryId)).thenReturn(feignResponse);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            adminService.resolveException(mockRequest, adminId);
        });
        assertTrue(exception.getMessage().contains("Invalid action"));
    }

    // ── 2. Generate Report (Testing complex nested map iteration) ─────────

    @Test
    void generateReport_Success_ShouldCalculateCountsCorrectly() {
        // Arrange
        ReportRequest mockRequest = mock(ReportRequest.class);
        when(mockRequest.getReportType()).thenReturn("DAILY_SUMMARY");
        when(mockRequest.getDateFrom()).thenReturn(LocalDate.now().minusDays(1));
        when(mockRequest.getDateTo()).thenReturn(LocalDate.now());

        // Simulate a page of deliveries coming back from Feign
        Map<String, Object> delivery1 = Map.of("status", "DELIVERED");
        Map<String, Object> delivery2 = Map.of("status", "FAILED");
        Map<String, Object> delivery3 = Map.of("status", "IN_TRANSIT"); // Not an exception, not delivered
        List<Map<String, Object>> contentList = List.of(delivery1, delivery2, delivery3);
        
        Map<String, Object> dataMap = Map.of("content", contentList);
        Map<String, Object> feignResponse = Map.of("data", dataMap);

        when(deliveryClient.getAllDeliveries(0, 1000)).thenReturn(feignResponse);
        when(reportRepository.save(any(Report.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Report result = adminService.generateReport(mockRequest, adminId);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalDeliveries());
        assertEquals(1, result.getDeliveredCount());
        assertEquals(1, result.getExceptionCount()); // Only the FAILED one counts as an exception
        assertEquals(adminId, result.getGeneratedBy());
        assertEquals("DAILY_SUMMARY", result.getReportType());
        
        verify(reportRepository).save(any(Report.class));
    }
}