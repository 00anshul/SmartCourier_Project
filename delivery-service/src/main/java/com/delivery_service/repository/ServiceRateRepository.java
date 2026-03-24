package com.delivery_service.repository;

import com.delivery_service.entity.ServiceRate;
import com.delivery_service.entity.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ServiceRateRepository extends JpaRepository<ServiceRate, Long> {

    Optional<ServiceRate> findByServiceType(ServiceType serviceType);
}