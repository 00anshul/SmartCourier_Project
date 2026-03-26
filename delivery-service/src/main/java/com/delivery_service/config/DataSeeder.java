package com.delivery_service.config;

import com.delivery_service.entity.ServiceRate;
import com.delivery_service.entity.ServiceType; // Make sure this matches your Enum package!
import com.delivery_service.repository.ServiceRateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);
    private final ServiceRateRepository serviceRateRepository;

    public DataSeeder(ServiceRateRepository serviceRateRepository) {
        this.serviceRateRepository = serviceRateRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        
        if (serviceRateRepository.count() == 0) {
            logger.info("Database is empty. Seeding initial Service Rates...");

            ServiceRate domestic = new ServiceRate();
            domestic.setServiceType(ServiceType.DOMESTIC);
            domestic.setBaseRate(new BigDecimal("50.00"));
            domestic.setRatePerKg(new BigDecimal("10.00"));
            domestic.setSurcharge(new BigDecimal("0.00"));
            // Note: updatedAt is handled automatically by your @PrePersist annotation, 
            // but we can set it here just to be safe before the first save.
            domestic.setUpdatedAt(LocalDateTime.now());

            ServiceRate express = new ServiceRate();
            express.setServiceType(ServiceType.EXPRESS);
            express.setBaseRate(new BigDecimal("100.00"));
            express.setRatePerKg(new BigDecimal("25.00"));
            express.setSurcharge(new BigDecimal("20.00"));
            express.setUpdatedAt(LocalDateTime.now());

            ServiceRate international = new ServiceRate();
            international.setServiceType(ServiceType.INTERNATIONAL);
            international.setBaseRate(new BigDecimal("500.00"));
            international.setRatePerKg(new BigDecimal("100.00"));
            international.setSurcharge(new BigDecimal("50.00"));
            international.setUpdatedAt(LocalDateTime.now());

            serviceRateRepository.saveAll(List.of(domestic, express, international));
            
            logger.info("Service Rates seeded successfully!");
        } else {
            logger.info("Service Rates already exist. Skipping seed.");
        }
    }
}