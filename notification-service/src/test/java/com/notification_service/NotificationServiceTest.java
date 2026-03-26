package com.notification_service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import com.notification_service.service.NotificationService;

// This extension tells Spring Boot to capture all SLF4J logs during the test
@ExtendWith(OutputCaptureExtension.class)
class NotificationServiceTest {

    private final NotificationService notificationService = new NotificationService();

    @Test
    void handleUserRegistered_ShouldLogWelcomeMessage(CapturedOutput output) {
        // Arrange
        String testEmail = "newuser@example.com";

        // Act
        notificationService.handleUserRegistered(testEmail);

        // Assert - Read the intercepted SLF4J logs instead of System.out
        String logs = output.getOut();
        assertTrue(logs.contains("=== NOTIFICATION ==="));
        assertTrue(logs.contains("New user registered: " + testEmail));
        assertTrue(logs.contains("Welcome email would be sent to: " + testEmail));
    }
}