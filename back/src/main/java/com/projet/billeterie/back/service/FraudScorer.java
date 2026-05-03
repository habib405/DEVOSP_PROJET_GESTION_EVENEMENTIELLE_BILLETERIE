package com.projet.billeterie.back.service;

import com.projet.billeterie.back.entity.Order;
import com.projet.billeterie.back.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FraudScorer {

    private final OrderRepository orderRepository;

    @Value("${fraud.threshold.soft:0.7}")
    private double softThreshold;

    @Value("${fraud.threshold.hard:0.9}")
    private double hardThreshold;

    @Value("${fraud.ood.window-minutes:10}")
    private int windowMinutes;

    @Value("${fraud.ood.count-threshold:5}")
    private int oodCountThreshold;

    @Value("${fraud.weight.amount:0.6}")
    private double weightAmount;

    @Value("${fraud.weight.frequency:0.4}")
    private double weightFrequency;

    @Value("${fraud.probability.scale:1.0}")
    private double probabilityScale;

    /**
     * Compute a combined anomaly score in [0,1].
     * Currently: weighted sum of normalized amount and frequency-based feature.
     */
    public double score(Order order) {
        double amountScore = normalizeAmount(order.getTotalAmount());
        double freqScore = computeFrequencyScore(order);
        double combined = clamp(weightAmount * amountScore + weightFrequency * freqScore);
        return combined;
    }

    private double normalizeAmount(double amount) {
        // Smooth normalization: saturates for very large amounts
        double x = amount / 1000.0; // 1000€ is a useful scale here
        return clamp(Math.tanh(x));
    }

    private double computeFrequencyScore(Order order) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(windowMinutes);
        List<Order> recent = orderRepository.findByUserId(order.getUserId()).stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(since))
                .collect(Collectors.toList());
        double count = recent.size();
        return clamp(count / (double) oodCountThreshold);
    }

    /**
     * Decide whether to block based on thresholds and probabilistic policy.
     * Returns true if the order should be blocked.
     */
    public boolean shouldBlock(Order order) {
        double s = score(order);
        if (s >= hardThreshold) return true; // hard block
        if (s < softThreshold) return false; // safe

        // soft region: probabilistic block (linear interpolation)
        double p = (s - softThreshold) / (hardThreshold - softThreshold);
        // scale probability to make tuning easier
        p = clamp(p * probabilityScale);
        double r = ThreadLocalRandom.current().nextDouble();
        return r < p;
    }

    private double clamp(double v) {
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }
}
