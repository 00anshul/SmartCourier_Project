package com.tracking_service.repository;

import com.tracking_service.entity.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {

    List<TrackingEvent> findByDeliveryIdOrderByCreatedAtAsc(Long deliveryId);

    List<TrackingEvent> findByTrackingNumberOrderByCreatedAtAsc(String trackingNumber);
}