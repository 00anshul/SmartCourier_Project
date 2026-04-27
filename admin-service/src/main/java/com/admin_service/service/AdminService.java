package com.admin_service.service;

import com.admin_service.config.DeliveryClient;
import com.admin_service.dto.HubRequest;
import com.admin_service.dto.ReportRequest;
import com.admin_service.dto.ResolveExceptionRequest;
import com.admin_service.entity.ExceptionLog;
import com.admin_service.entity.Hub;
import com.admin_service.entity.Report;
import com.admin_service.repository.ExceptionLogRepository;
import com.admin_service.repository.HubRepository;
import com.admin_service.repository.ReportRepository;

import io.micrometer.tracing.Tracer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    // 1. Initialize the Logger
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    @Autowired
    private HubRepository hubRepository;

    @Autowired
    private ExceptionLogRepository exceptionLogRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private DeliveryClient deliveryClient;

    // Hub management

    public Hub createHub(HubRequest request) {
        logger.info("Attempting to create new Hub: {} in {}", request.getName(), request.getCity());

        if (hubRepository.existsByNameAndCity(request.getName(), request.getCity())) {
            logger.warn("Hub creation failed: Hub {} already exists in {}", request.getName(), request.getCity());
            throw new RuntimeException("Hub already exists in this city");
        }

        Hub hub = new Hub();
        hub.setName(request.getName());
        hub.setCity(request.getCity());
        hub.setState(request.getState());
        hub.setPincode(request.getPincode());
        hub.setContactPhone(request.getContactPhone());

        Hub savedHub = hubRepository.save(hub);
        logger.info("Successfully created Hub with ID: {}", savedHub.getId());
        return savedHub;
    }

    public List<Hub> getAllHubs() {
        return hubRepository.findAll();
    }

    public List<Hub> getActiveHubs() {
        return hubRepository.findByIsActive(true);
    }

    public Hub toggleHubStatus(Long hubId) {
        logger.info("Attempting to toggle active status for Hub ID: {}", hubId);
        
        Hub hub = hubRepository.findById(hubId)
                .orElseThrow(() -> {
                    logger.error("Hub status toggle failed: Hub ID {} not found", hubId);
                    return new RuntimeException("Hub not found");
                });
                
        hub.setIsActive(!hub.getIsActive());
        Hub updatedHub = hubRepository.save(hub);
        
        logger.info("Successfully toggled Hub ID: {} to Active: {}", hubId, updatedHub.getIsActive());
        return updatedHub;
    }

    // Delivery monitoring

    public Map<String, Object> getAllDeliveries(int page, int size) {
        logger.info("Fetching deliveries from DeliveryService via Feign (page: {}, size: {})", page, size);
        return deliveryClient.getAllDeliveries(page, size);
    }

    public Map<String, Object> getDeliveryById(Long id) {
        logger.info("Fetching Delivery ID: {} from DeliveryService via Feign", id);
        return deliveryClient.getDeliveryById(id);
    }

    // Exception resolution

    @SuppressWarnings("unchecked")
    public ExceptionLog resolveException(ResolveExceptionRequest request, Long adminId) {
        logger.info("Admin ID: {} attempting to resolve exception for Delivery ID: {} with action: {}", 
                adminId, request.getDeliveryId(), request.getAction());

        Map<String, Object> deliveryResponse = deliveryClient.getDeliveryById(request.getDeliveryId());

        if (deliveryResponse == null) {
            logger.error("Resolution failed: Feign client returned null for Delivery ID: {}", request.getDeliveryId());
            throw new RuntimeException("Delivery not found");
        }

        Map<String, Object> delivery = (Map<String, Object>) deliveryResponse.get("data");

        if (delivery == null) {
            logger.error("Resolution failed: 'data' object missing in Feign response for Delivery ID: {}", request.getDeliveryId());
            throw new RuntimeException("Delivery data not found");
        }

        String currentStatus = (String) delivery.get("status");

        String newStatus = switch (request.getAction().toUpperCase()) {
            case "RETRY"   -> "OUT_FOR_DELIVERY";
            case "RETURN"  -> "RETURNED";
            case "REFUND"  -> "REFUNDED";
            default -> {
                logger.warn("Resolution failed: Invalid action '{}' requested for Delivery ID: {}", request.getAction(), request.getDeliveryId());
                throw new RuntimeException("Invalid action. Use RETRY, RETURN or REFUND");
            }
        };

        // Update delivery service via Feign
        deliveryClient.updateDeliveryStatus(request.getDeliveryId(), newStatus);
        logger.info("Successfully updated Delivery ID: {} to status: {} via Feign", request.getDeliveryId(), newStatus);

        ExceptionLog log = new ExceptionLog();
        log.setDeliveryId(request.getDeliveryId());
        log.setAction(request.getAction().toUpperCase());
        log.setPreviousStatus(currentStatus);
        log.setNewStatus(newStatus);
        log.setNote(request.getNote());
        log.setResolvedBy(adminId);

        ExceptionLog savedLog = exceptionLogRepository.save(log);
        logger.info("Successfully saved Exception Log ID: {}", savedLog.getId());
        return savedLog;
    }

    public List<ExceptionLog> getExceptionLogs(Long deliveryId) {
        return exceptionLogRepository.findByDeliveryId(deliveryId);
    }

    // Reports

    public Report generateReport(ReportRequest request, Long adminId) {
        logger.info("Admin ID: {} initiating generation of {} report", adminId, request.getReportType());

        int total      = 0;
        int delivered  = 0;
        int exceptions = 0;

        Map<String, Object> response = deliveryClient.getAllDeliveries(0, 1000);

        if (response != null && response.containsKey("data")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = (Map<String, Object>) response.get("data");
            
            if (dataMap != null && dataMap.containsKey("content")) {
                List<?> list = (List<?>) dataMap.get("content");
                total = list.size();
                
                for (Object item : list) {
                    if (item instanceof Map<?, ?> delivery) {
                        String status = (String) delivery.get("status");
                        if ("DELIVERED".equals(status)) {
                            delivered++;
                        } else if ("FAILED".equals(status)
                                || "DELAYED".equals(status)
                                || "RETURNED".equals(status)) {
                            exceptions++;
                        }
                    }
                }
            }
        } else {
            logger.warn("Report generation: DeliveryService returned empty or null data via Feign.");
        }

        Report report = new Report();
        report.setReportType(request.getReportType());
        report.setGeneratedBy(adminId);
        report.setDateFrom(request.getDateFrom());
        report.setDateTo(request.getDateTo());
        report.setTotalDeliveries(total);
        report.setDeliveredCount(delivered);
        report.setExceptionCount(exceptions);

        Report savedReport = reportRepository.save(report);
        logger.info("Successfully generated report ID: {} (Total: {}, Delivered: {}, Exceptions: {})", 
                savedReport.getId(), total, delivered, exceptions);
        return savedReport;
    }

    public List<Report> getReports(Long adminId) {
        return reportRepository.findByGeneratedByOrderByGeneratedAtDesc(adminId);
    }

    // Dashboard summary

    public Map<String, Object> getDashboard() {
        logger.info("Compiling Admin Dashboard summary metrics");
       
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalHubs", hubRepository.count());
        dashboard.put("activeHubs", hubRepository.findByIsActive(true).size());
        dashboard.put("totalExceptions", exceptionLogRepository.count());

        try {
            Map<String, Object> response = deliveryClient.getAllDeliveries(0, 1);
            if (response != null && response.containsKey("data")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = (Map<String, Object>) response.get("data");
                if (dataMap != null) {
                    dashboard.put("totalDeliveries", dataMap.get("totalElements"));
                }
            }
        } catch (Exception e) {
            logger.error("Failed to fetch total deliveries for dashboard via Feign", e);
            dashboard.put("totalDeliveries", 0); // Graceful degradation
        }

        return dashboard;
    }
}