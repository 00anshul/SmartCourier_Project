package com.admin_service.repository;

import com.admin_service.entity.ExceptionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExceptionLogRepository extends JpaRepository<ExceptionLog, Long> {

    List<ExceptionLog> findByDeliveryId(Long deliveryId);
}