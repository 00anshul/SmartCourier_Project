package com.delivery_service.service;

import com.delivery_service.config.RabbitMQConfig;
import com.delivery_service.dto.CreateDeliveryRequest;
import com.delivery_service.dto.SchedulePickupRequest;
import com.delivery_service.entity.*;
import com.delivery_service.entity.Package;
import com.delivery_service.repository.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
public class DeliveryService {

    // 1. Initialize the Logger
    private static final Logger logger = LoggerFactory.getLogger(DeliveryService.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PickupRepository pickupRepository;

    @Autowired
    private ServiceRateRepository serviceRateRepository;

    @Transactional
    public Delivery createDelivery(CreateDeliveryRequest request, Long customerId) {
        logger.info("Initiating delivery creation for Customer ID: {}, Service Type: {}", customerId, request.getServiceType());

        ServiceRate rate = serviceRateRepository
                .findByServiceType(request.getServiceType())
                .orElseThrow(() -> {
                    logger.error("Delivery creation failed: Service rate not configured for {}", request.getServiceType());
                    return new RuntimeException("Service rate not configured for: " + request.getServiceType());
                });

        BigDecimal charge = calculateCharge(rate, request.getPackageDetails().getWeightKg());

        Delivery delivery = new Delivery();
        delivery.setCustomerId(customerId);
        delivery.setServiceType(request.getServiceType());
        delivery.setStatus(DeliveryStatus.BOOKED);
        delivery.setTotalCharge(charge);
        delivery.setNotes(request.getNotes());
        delivery.setTrackingNumber(generateTrackingNumber());

        Delivery saved = deliveryRepository.save(delivery);

        Package parcel = new Package();
        parcel.setDelivery(saved);
        parcel.setWeightKg(request.getPackageDetails().getWeightKg());
        parcel.setLengthCm(request.getPackageDetails().getLengthCm());
        parcel.setWidthCm(request.getPackageDetails().getWidthCm());
        parcel.setHeightCm(request.getPackageDetails().getHeightCm());
        parcel.setDescription(request.getPackageDetails().getDescription());
        packageRepository.save(parcel);

        Address sender = mapAddress(request.getSenderAddress(), saved, "SENDER");
        Address receiver = mapAddress(request.getReceiverAddress(), saved, "RECEIVER");
        addressRepository.save(sender);
        addressRepository.save(receiver);

        logger.info("Successfully created delivery with Tracking Number: {}", saved.getTrackingNumber());
        return saved;
    }

    public Delivery getDeliveryById(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Delivery lookup failed: No delivery found with ID: {}", id);
                    return new RuntimeException("Delivery not found with id: " + id);
                });
    }

    public Delivery getDeliveryByTrackingNumber(String trackingNumber) {
        return deliveryRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> {
                    logger.warn("Delivery lookup failed: No delivery found with Tracking Number: {}", trackingNumber);
                    return new RuntimeException("Delivery not found with tracking number: " + trackingNumber);
                });
    }

    public Page<Delivery> getMyDeliveries(Long customerId, Pageable pageable) {
        logger.info("Fetching deliveries for Customer ID: {}", customerId);
        return deliveryRepository.findByCustomerId(customerId, pageable);
    }

    public Page<Delivery> getAllDeliveries(Pageable pageable) {
        return deliveryRepository.findAll(pageable);
    }

    @Transactional
    public Pickup schedulePickup(Long deliveryId, SchedulePickupRequest request, Long customerId) {
        logger.info("Attempting to schedule pickup for Delivery ID: {} by Customer ID: {}", deliveryId, customerId);

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> {
                    logger.warn("Pickup scheduling failed: Delivery not found with ID: {}", deliveryId);
                    return new RuntimeException("Delivery not found");
                });

        if (!delivery.getCustomerId().equals(customerId)) {
            logger.warn("Security warning: Customer {} attempted to schedule pickup for unauthorized Delivery {}", customerId, deliveryId);
            throw new RuntimeException("You are not authorized to schedule pickup for this delivery");
        }

        if (pickupRepository.findByDeliveryId(deliveryId).isPresent()) {
            logger.warn("Pickup scheduling failed: Pickup already exists for Delivery {}", deliveryId);
            throw new RuntimeException("Pickup already scheduled for this delivery");
        }

        LocalDate cutoff = LocalDate.now();
        if (!request.getScheduledDate().isAfter(cutoff)) {
            logger.warn("Pickup scheduling failed: Requested date {} is not in the future for Delivery {}", request.getScheduledDate(), deliveryId);
            throw new RuntimeException("Pickup must be scheduled for a future date");
        }

        Pickup pickup = new Pickup();
        pickup.setDelivery(delivery);
        pickup.setScheduledDate(request.getScheduledDate());
        pickup.setSlot(request.getSlot());
        pickup.setStatus("PENDING");

        Pickup savedPickup = pickupRepository.save(pickup);
        logger.info("Successfully scheduled pickup for Delivery ID: {} on {}", deliveryId, request.getScheduledDate());
        return savedPickup;
    }

    @Transactional
    public Delivery updateStatus(Long deliveryId, String status) {
        logger.info("Attempting to update status for Delivery ID: {} to {}", deliveryId, status);

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> {
                    logger.warn("Status update failed: Delivery not found with ID: {}", deliveryId);
                    return new RuntimeException("Delivery not found");
                });

        delivery.setStatus(DeliveryStatus.valueOf(status));
        Delivery saved = deliveryRepository.save(delivery);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DELIVERY_STATUS_EXCHANGE,
                RabbitMQConfig.DELIVERY_STATUS_KEY,
                saved.getId() + ":" + saved.getTrackingNumber() + ":" + status
        );

        logger.info("Successfully updated status to {} and published to RabbitMQ for Tracking Number: {}", status, saved.getTrackingNumber());
        return saved;
    }

    public BigDecimal getQuote(String serviceType, BigDecimal weightKg) {
        logger.info("Generating quote for Service Type: {}, Weight: {}kg", serviceType, weightKg);
        
        ServiceRate rate = serviceRateRepository
                .findByServiceType(ServiceType.valueOf(serviceType))
                .orElseThrow(() -> {
                    logger.error("Quote generation failed: Service rate not configured for {}", serviceType);
                    return new RuntimeException("Service rate not configured");
                });
        return calculateCharge(rate, weightKg);
    }

    private BigDecimal calculateCharge(ServiceRate rate, BigDecimal weightKg) {
        return rate.getBaseRate()
                .add(weightKg.multiply(rate.getRatePerKg()))
                .add(rate.getSurcharge());
    }

    private String generateTrackingNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return "SC-" + date + "-" + sb;
    }

    private Address mapAddress(com.delivery_service.dto.AddressRequest dto, Delivery delivery, String type) {
        Address address = new Address();
        address.setDelivery(delivery);
        address.setType(type);
        address.setFullName(dto.getFullName());
        address.setPhone(dto.getPhone());
        address.setStreet(dto.getStreet());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setPincode(dto.getPincode());
        address.setCountry(dto.getCountry());
        return address;
    }
}