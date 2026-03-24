package com.delivery_service.repository;

import com.delivery_service.entity.Delivery;
import com.delivery_service.entity.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Page<Delivery> findByCustomerId(Long customerId, Pageable pageable);

    Optional<Delivery> findByTrackingNumber(String trackingNumber);

    Page<Delivery> findByStatus(DeliveryStatus status, Pageable pageable);

    List<Delivery> findByCustomerIdAndStatus(Long customerId, DeliveryStatus status);
}