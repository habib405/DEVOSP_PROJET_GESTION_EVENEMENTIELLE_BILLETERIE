package com.projet.billeterie.back.repository;

import com.projet.billeterie.back.entity.FraudMonitoring;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FraudMonitoringRepository extends JpaRepository<FraudMonitoring, UUID> {
    List<FraudMonitoring> findByOrderId(UUID orderId);
}
