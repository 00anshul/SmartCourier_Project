package com.admin_service.repository;

import com.admin_service.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByGeneratedByOrderByGeneratedAtDesc(Long generatedBy);
}