package com.admin_service.controller;

import com.admin_service.dto.ApiResponse;
import com.admin_service.dto.HubRequest;
import com.admin_service.dto.ReportRequest;
import com.admin_service.dto.ResolveExceptionRequest;
import com.admin_service.entity.ExceptionLog;
import com.admin_service.entity.Hub;
import com.admin_service.entity.Report;
import com.admin_service.service.AdminService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        Map<String, Object> dashboard = adminService.getDashboard();
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Dashboard data", dashboard));
    }

    // Hub endpoints

    @PostMapping("/hubs")
    public ResponseEntity<ApiResponse<Hub>> createHub(
            @Valid @RequestBody HubRequest request) {
        Hub hub = adminService.createHub(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true,
                        "Hub created successfully", hub));
    }

    @GetMapping("/hubs")
    public ResponseEntity<ApiResponse<List<Hub>>> getAllHubs() {
        List<Hub> hubs = adminService.getAllHubs();
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Hubs fetched", hubs));
    }

    @PutMapping("/hubs/{id}/toggle")
    public ResponseEntity<ApiResponse<Hub>> toggleHub(
            @PathVariable Long id) {
        Hub hub = adminService.toggleHubStatus(id);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Hub status updated", hub));
    }

    // Delivery monitoring

    @GetMapping("/deliveries")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllDeliveries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Map<String, Object> deliveries = adminService
                .getAllDeliveries(page, size);
        return ResponseEntity.ok(
                new ApiResponse<>(true,
                        "Deliveries fetched", deliveries));
    }

    @GetMapping("/deliveries/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDelivery(
            @PathVariable Long id) {
        Map<String, Object> delivery = adminService.getDeliveryById(id);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Delivery found", delivery));
    }

    // Exception resolution

    @PostMapping("/deliveries/resolve")
    public ResponseEntity<ApiResponse<ExceptionLog>> resolveException(
            @Valid @RequestBody ResolveExceptionRequest request,
            HttpServletRequest httpRequest) {
        Long adminId = (Long) httpRequest.getAttribute("userId");
        ExceptionLog log = adminService.resolveException(request, adminId);
        return ResponseEntity.ok(
                new ApiResponse<>(true,
                        "Exception resolved successfully", log));
    }

    @GetMapping("/deliveries/{id}/exceptions")
    public ResponseEntity<ApiResponse<List<ExceptionLog>>> getExceptions(
            @PathVariable Long id) {
        List<ExceptionLog> logs = adminService.getExceptionLogs(id);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Exception logs found", logs));
    }

    // Reports

    @PostMapping("/reports")
    public ResponseEntity<ApiResponse<Report>> generateReport(
            @Valid @RequestBody ReportRequest request,
            HttpServletRequest httpRequest) {
        Long adminId = (Long) httpRequest.getAttribute("userId");
        Report report = adminService.generateReport(request, adminId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true,
                        "Report generated", report));
    }

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<List<Report>>> getReports(
            HttpServletRequest httpRequest) {
        Long adminId = (Long) httpRequest.getAttribute("userId");
        List<Report> reports = adminService.getReports(adminId);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Reports fetched", reports));
    }
}