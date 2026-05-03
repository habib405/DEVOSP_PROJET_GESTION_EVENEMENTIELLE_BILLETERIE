package com.projet.billeterie.back.service;

import com.projet.billeterie.back.dto.FraudMonitoringRequest;
import com.projet.billeterie.back.entity.FraudMonitoring;
import com.projet.billeterie.back.exception.ResourceNotFoundException;
import com.projet.billeterie.back.repository.FraudMonitoringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FraudMonitoringService {

    private final FraudMonitoringRepository fraudRepository;

    @Transactional
    public FraudMonitoring create(FraudMonitoringRequest request) {
        FraudMonitoring record = FraudMonitoring.builder()
                .orderId(request.getOrderId())
                .scoreAnomalie(request.getScoreAnomalie())
                .typeFraude(request.getTypeFraude())
                .detectedAt(request.getDetectedAt() != null ? request.getDetectedAt() : LocalDateTime.now())
                .build();
        return fraudRepository.save(record);
    }

    public List<FraudMonitoring> findAll() {
        return fraudRepository.findAll();
    }

    public List<FraudMonitoring> findByOrder(UUID orderId) {
        return fraudRepository.findByOrderId(orderId);
    }

    public List<FraudMonitoring> findPendingReview() {
        return fraudRepository.findByFalsePositiveFalse();
    }

    public FraudMonitoring findById(UUID id) {
        return fraudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FraudMonitoring record not found: " + id));
    }

    public Page<FraudMonitoring> findPendingReview(int page, int size) {
        return fraudRepository.findByFalsePositiveFalse(PageRequest.of(page, size));
    }

    @Transactional
    public FraudMonitoring markFalsePositive(UUID id, String comment) {
        FraudMonitoring rec = fraudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FraudMonitoring record not found: " + id));
        rec.setFalsePositive(true);
        rec.setReviewedAt(LocalDateTime.now());
        rec.setReviewComment(comment);
        return fraudRepository.save(rec);
    }
}
