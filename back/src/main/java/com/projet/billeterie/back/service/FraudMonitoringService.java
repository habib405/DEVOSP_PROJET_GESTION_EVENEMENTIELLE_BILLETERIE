package com.projet.billeterie.back.service;

import com.projet.billeterie.back.dto.FraudMonitoringRequest;
import com.projet.billeterie.back.entity.*;
import com.projet.billeterie.back.exception.FraudDetectedException;
import com.projet.billeterie.back.exception.ResourceNotFoundException;
import com.projet.billeterie.back.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudMonitoringService {

    private final FraudMonitoringRepository fraudRepository;
    private final OrderRepository orderRepository;
    private final RegistrationRepository registrationRepository;
    private final CheckInRepository checkInRepository;

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

    /**
     * Analyse une commande pour détecter une fraude potentielle.
     * Ex: Trop de commandes en peu de temps pour un même utilisateur.
     */
    @Transactional
    public void analyzeOrder(Order order) {
        float scoreTotal = 0.0f;
        StringBuilder motifs = new StringBuilder();

        // Règle 1 : Vélocité d'achat (Plus de 5 commandes CONFIRMED en 10 minutes)
        long recentOrders = orderRepository.countByUserIdAndStatusAndCreatedAtAfter(
                order.getUserId(),
                OrderStatus.CONFIRMED,
                LocalDateTime.now().minusMinutes(10)
        );

        if (recentOrders > 5) {
            scoreTotal += 0.8f;
            motifs.append("VELOCITE_ACHAT_ELEVEE ");
        }

        // Règle 2 : Montant anormalement élevé
        if (order.getTotalAmount() > 1000) {
            scoreTotal += 0.5f;
            motifs.append("MONTANT_ELEVÉ ");
        }

        // --- Action post-analyse ---
        if (scoreTotal > 0) {
            log.warn("Suspicion de fraude pour la commande {} (Score: {}). Motifs: {}", 
                     order.getId(), scoreTotal, motifs.toString());
            
            // On trace l'anomalie en base
            create(FraudMonitoringRequest.builder()
                    .orderId(order.getId())
                    .scoreAnomalie(scoreTotal)
                    .typeFraude(motifs.toString().trim())
                    .build());
        }

       // --- Le "Kill Switch" ---
        // Si le score atteint ou dépasse 1.0, on bloque immédiatement la transaction
        if (scoreTotal >= 1.0f) {
            log.error("Transaction {} bloquée. Score de fraude critique dépassé.", order.getId());
            
            // On utilise notre nouvelle exception personnalisée !
            throw new FraudDetectedException("Transaction refusée par le système de sécurité. Veuillez contacter le support.");
        }
    }
    /**
     * Détecte une fraude au scan (Double scan).
     */
    @Transactional
    public void detectDoubleScan(Registration registration, UUID staffId) {
        boolean alreadyCheckedIn = checkInRepository.findAll().stream()
                .anyMatch(c -> c.getRegistration().getId().equals(registration.getId()) && Boolean.TRUE.equals(c.getIsValid()));

        if (alreadyCheckedIn) {
            log.error("Fraude détectée: Double scan pour le billet {}", registration.getId());
            create(FraudMonitoringRequest.builder()
                    .orderId(registration.getOrderId())
                    .scoreAnomalie(1.0f)
                    .typeFraude("DOUBLE_SCAN_TICKET")
                    .build());
        }
    }

    public List<FraudMonitoring> findAll() {
        return fraudRepository.findAll();
    }
// ...


    public List<FraudMonitoring> findByOrder(UUID orderId) {
        return fraudRepository.findByOrderId(orderId);
    }

    public FraudMonitoring findById(UUID id) {
        return fraudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FraudMonitoring record not found: " + id));
    }
}
