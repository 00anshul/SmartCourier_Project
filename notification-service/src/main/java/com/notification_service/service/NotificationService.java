package com.notification_service.service;

import com.notification_service.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @RabbitListener(queues = RabbitMQConfig.USER_REGISTERED_QUEUE)
    public void handleUserRegistered(String email) {
        logger.info("=== NOTIFICATION ===");
        logger.info("New user registered: {}", email);
        logger.info("Welcome email would be sent to: {}", email);
        logger.info("====================");
    }
}