package com.notification_service.service;

import com.notification_service.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @RabbitListener(queues = RabbitMQConfig.USER_REGISTERED_QUEUE)
    public void handleUserRegistered(String email) {
        System.out.println("=== NOTIFICATION ===");
        System.out.println("New user registered: " + email);
        System.out.println("Welcome email would be sent to: " + email);
        System.out.println("====================");
    }
}