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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

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
        if (hubRepository.existsByNameAndCity(
                request.getName(), request.getCity())) {
            throw new RuntimeException(
                    "Hub already exists in this city");
        }

        Hub hub = new Hub();
        hub.setName(request.getName());
        hub.setCity(request.getCity());
        hub.setState(request.getState());
        hub.setPincode(request.getPincode());
        hub.setContactPhone(request.getContactPhone());

        return hubRepository.save(hub);
    }

    public List<Hub> getAllHubs() {
        return hubRepository.findAll();
    }

    public List<Hub> getActiveHubs() {
        return hubRepository.findByIsActive(true);
    }

    public Hub toggleHubStatus(Long hubId) {
        Hub hub = hubRepository.findById(hubId)
                .orElseThrow(() -> new RuntimeException(
                        "Hub not found"));
        hub.setIsActive(!hub.getIsActive());
        return hubRepository.save(hub);
    }

    // Delivery monitoring

    public Map<String, Object> getAllDeliveries(int page, int size) {
        return deliveryClient.getAllDeliveries(page, size);
    }

    public Map<String, Object> getDeliveryById(Long id) {
        return deliveryClient.getDeliveryById(id);
    }

    // Exception resolution

    public ExceptionLog resolveException(
            ResolveExceptionRequest request, Long adminId) {

    	Map<String, Object> deliveryResponse = deliveryClient
    	        .getDeliveryById(request.getDeliveryId());

    	if (deliveryResponse == null) {
    	    throw new RuntimeException("Delivery not found");
    	}

    	Map<String, Object> delivery = (Map<String, Object>) deliveryResponse.get("data");

    	if (delivery == null) {
    	    throw new RuntimeException("Delivery data not found");
    	}

    	String currentStatus = (String) delivery.get("status");

        String newStatus = switch (request.getAction().toUpperCase()) {
            case "RETRY"   -> "OUT_FOR_DELIVERY";
            case "RETURN"  -> "RETURNED";
            case "REFUND"  -> "REFUNDED";
            default -> throw new RuntimeException(
                    "Invalid action. Use RETRY, RETURN or REFUND");
        };

        deliveryClient.updateDeliveryStatus(
                request.getDeliveryId(), newStatus);

        ExceptionLog log = new ExceptionLog();
        log.setDeliveryId(request.getDeliveryId());
        log.setAction(request.getAction().toUpperCase());
        log.setPreviousStatus(currentStatus);
        log.setNewStatus(newStatus);
        log.setNote(request.getNote());
        log.setResolvedBy(adminId);

        return exceptionLogRepository.save(log);
    }

    public List<ExceptionLog> getExceptionLogs(Long deliveryId) {
        return exceptionLogRepository.findByDeliveryId(deliveryId);
    }

    // Reports

    public Report generateReport(ReportRequest request, Long adminId) {

        int total      = 0;
        int delivered  = 0;
        int exceptions = 0;

        Map<String, Object> response = deliveryClient
                .getAllDeliveries(0, 1000);

        if (response != null && response.containsKey("data")) {
            Map<String, Object> dataMap =
                    (Map<String, Object>) response.get("data");
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
        }

        Report report = new Report();
        report.setReportType(request.getReportType());
        report.setGeneratedBy(adminId);
        report.setDateFrom(request.getDateFrom());
        report.setDateTo(request.getDateTo());
        report.setTotalDeliveries(total);
        report.setDeliveredCount(delivered);
        report.setExceptionCount(exceptions);

        return reportRepository.save(report);
    }

    public List<Report> getReports(Long adminId) {
        return reportRepository
                .findByGeneratedByOrderByGeneratedAtDesc(adminId);
    }

    // Dashboard summary

    public Map<String, Object> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalHubs", hubRepository.count());
        dashboard.put("activeHubs",
                hubRepository.findByIsActive(true).size());
        dashboard.put("totalExceptions",
                exceptionLogRepository.count());

        Map<String, Object> response = deliveryClient
                .getAllDeliveries(0, 1);
        if (response != null && response.containsKey("data")) {
            Map<String, Object> dataMap =
                    (Map<String, Object>) response.get("data");
            if (dataMap != null) {
                dashboard.put("totalDeliveries",
                        dataMap.get("totalElements"));
            }
        }

        return dashboard;
    }
}