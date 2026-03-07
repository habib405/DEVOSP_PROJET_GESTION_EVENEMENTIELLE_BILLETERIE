package com.projet.gestion.evenementielle.repository;

import com.projet.gestion.evenementielle.entity.FraudMonitoring;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FraudMonitoringRepository extends JpaRepository<FraudMonitoring, UUID> {
    List<FraudMonitoring> findByOrderId(UUID orderId);
}
