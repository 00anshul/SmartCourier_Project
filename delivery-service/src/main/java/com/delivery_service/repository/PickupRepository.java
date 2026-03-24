package com.delivery_service.repository;

import com.delivery_service.entity.Pickup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PickupRepository extends JpaRepository<Pickup, Long> {

    Optional<Pickup> findByDeliveryId(Long deliveryId);
}