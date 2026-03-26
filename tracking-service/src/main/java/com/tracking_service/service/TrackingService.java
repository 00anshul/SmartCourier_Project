package com.tracking_service.service;

import com.tracking_service.config.RabbitMQConfig;
import com.tracking_service.dto.DeliveryProofRequest;
import com.tracking_service.dto.TrackingEventRequest;
import com.tracking_service.entity.DeliveryProof;
import com.tracking_service.entity.Document;
import com.tracking_service.entity.TrackingEvent;
import com.tracking_service.repository.DeliveryProofRepository;
import com.tracking_service.repository.DocumentRepository;
import com.tracking_service.repository.TrackingEventRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class TrackingService {

    // 1. Initialize the Logger for this service
    private static final Logger logger = LoggerFactory.getLogger(TrackingService.class);

    @Autowired
    private TrackingEventRepository trackingEventRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DeliveryProofRepository deliveryProofRepository;

    // Consumes delivery status change messages from RabbitMQ
    @RabbitListener(queues = RabbitMQConfig.DELIVERY_STATUS_QUEUE)
    public void consumeDeliveryStatusChange(String message) {
        String[] parts = message.split(":");
        
        // 1. If the message format is wrong, reject it to the DLQ immediately
        if (parts.length < 3) {
            logger.warn("Invalid RabbitMQ message format received. Routing to DLQ: {}", message);
            throw new AmqpRejectAndDontRequeueException("Invalid message format: " + message);
        }

        try {
            Long deliveryId      = Long.parseLong(parts[0]);
            String trackingNumber = parts[1];
            String status         = parts[2];

            logger.info("Processing status update from RabbitMQ - Tracking: {}, Status: {}", trackingNumber, status);

            TrackingEvent event = new TrackingEvent();
            event.setDeliveryId(deliveryId);
            event.setTrackingNumber(trackingNumber);
            event.setStatus(status);
            event.setLocationDescription("Status updated to " + status);

            trackingEventRepository.save(event);
            logger.info("Successfully saved tracking event for Delivery ID: {}", deliveryId);

        } catch (Exception e) {
            // 2. If parsing fails or the DB crashes, log the stack trace and route to DLQ
            logger.error("Failed to process RabbitMQ message: {}", message, e);
            throw new AmqpRejectAndDontRequeueException("Failed to process message: " + message, e);
        }
    }

    public TrackingEvent createTrackingEvent(TrackingEventRequest request, Long adminId) {
        TrackingEvent event = new TrackingEvent();
        event.setDeliveryId(request.getDeliveryId());
        event.setTrackingNumber(request.getTrackingNumber());
        event.setStatus(request.getStatus());
        event.setHubId(request.getHubId());
        event.setLocationDescription(request.getLocationDescription());
        event.setRemarks(request.getRemarks());
        event.setCreatedBy(adminId);

        TrackingEvent savedEvent = trackingEventRepository.save(event);
        logger.info("Manual tracking event created for Tracking Number: {} by Admin ID: {}", request.getTrackingNumber(), adminId);
        return savedEvent;
    }

    public List<TrackingEvent> getEventsByTrackingNumber(String trackingNumber) {
        return trackingEventRepository.findByTrackingNumberOrderByCreatedAtAsc(trackingNumber);
    }

    public List<TrackingEvent> getEventsByDeliveryId(Long deliveryId) {
        return trackingEventRepository.findByDeliveryIdOrderByCreatedAtAsc(deliveryId);
    }

    public Document uploadDocument(Long deliveryId, MultipartFile file, Long userId) throws IOException {
        Long count = documentRepository.countByDeliveryId(deliveryId);
        if (count >= 3) {
            logger.warn("Upload rejected: Maximum 3 documents reached for Delivery ID: {}", deliveryId);
            throw new RuntimeException("Maximum 3 documents allowed per delivery");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            logger.warn("Upload rejected: File size exceeds 5MB limit. File: {}, Size: {} bytes", file.getOriginalFilename(), file.getSize());
            throw new RuntimeException("File size exceeds 5MB limit");
        }

        String uploadDir = "uploads/" + deliveryId + "/";
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String filePath = uploadDir + fileName;

        file.transferTo(new File(filePath));

        String fileType = getFileExtension(file.getOriginalFilename()).toUpperCase();
        if (!fileType.equals("PDF") && !fileType.equals("JPG") && !fileType.equals("PNG")) {
            logger.warn("Upload rejected: Invalid file type ({}) attempted for Delivery ID: {}", fileType, deliveryId);
            throw new RuntimeException("Only PDF, JPG and PNG files are allowed");
        }

        Document document = new Document();
        document.setDeliveryId(deliveryId);
        document.setFileName(file.getOriginalFilename());
        document.setFilePath(filePath);
        document.setFileType(fileType);
        document.setFileSizeBytes(file.getSize());
        document.setUploadedBy(userId);

        Document savedDoc = documentRepository.save(document);
        logger.info("Document successfully uploaded and saved for Delivery ID: {}", deliveryId);
        return savedDoc;
    }

    public List<Document> getDocumentsByDeliveryId(Long deliveryId) {
        return documentRepository.findByDeliveryId(deliveryId);
    }

    public DeliveryProof addDeliveryProof(DeliveryProofRequest request, Long adminId) {
        if (deliveryProofRepository.findByDeliveryId(request.getDeliveryId()).isPresent()) {
            logger.warn("Delivery proof addition rejected: Proof already exists for Delivery ID: {}", request.getDeliveryId());
            throw new RuntimeException("Proof already exists for this delivery");
        }

        DeliveryProof proof = new DeliveryProof();
        proof.setDeliveryId(request.getDeliveryId());
        proof.setReceivedBy(request.getReceivedBy());
        proof.setRemarks(request.getRemarks());

        DeliveryProof savedProof = deliveryProofRepository.save(proof);
        logger.info("Delivery proof successfully added for Delivery ID: {} by Admin ID: {}", request.getDeliveryId(), adminId);
        return savedProof;
    }

    public DeliveryProof getProofByDeliveryId(Long deliveryId) {
        return deliveryProofRepository.findByDeliveryId(deliveryId)
                .orElseThrow(() -> {
                    logger.error("Delivery proof not found for Delivery ID: {}", deliveryId);
                    return new RuntimeException("No proof found for delivery: " + deliveryId);
                });
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "";
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}