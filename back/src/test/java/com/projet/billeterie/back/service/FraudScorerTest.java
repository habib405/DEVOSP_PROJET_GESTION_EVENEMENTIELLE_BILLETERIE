package com.projet.billeterie.back.service;

import com.projet.billeterie.back.entity.Order;
import com.projet.billeterie.back.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FraudScorerTest {

    private OrderRepository repo;
    private FraudScorer scorer;

    @BeforeEach
    void setUp() {
        repo = mock(OrderRepository.class);
        scorer = new FraudScorer(repo);

        // sensible defaults
        ReflectionTestUtils.setField(scorer, "softThreshold", 0.7);
        ReflectionTestUtils.setField(scorer, "hardThreshold", 0.9);
        ReflectionTestUtils.setField(scorer, "windowMinutes", 10);
        ReflectionTestUtils.setField(scorer, "oodCountThreshold", 5);
        ReflectionTestUtils.setField(scorer, "weightAmount", 0.6);
        ReflectionTestUtils.setField(scorer, "weightFrequency", 0.4);
    }

    @Test
    void score_amountDominant() {
        ReflectionTestUtils.setField(scorer, "weightAmount", 1.0);
        ReflectionTestUtils.setField(scorer, "weightFrequency", 0.0);

        Order o = Order.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .totalAmount(2000f)
                .createdAt(LocalDateTime.now())
                .build();

        double s = scorer.score(o);
        assertTrue(s > 0.95, "expected high score for large amount");
    }

    @Test
    void score_frequencyDominant() {
        ReflectionTestUtils.setField(scorer, "weightAmount", 0.0);
        ReflectionTestUtils.setField(scorer, "weightFrequency", 1.0);
        ReflectionTestUtils.setField(scorer, "oodCountThreshold", 5);

        UUID user = UUID.randomUUID();
        Order o = Order.builder()
                .id(UUID.randomUUID())
                .userId(user)
                .totalAmount(10f)
                .createdAt(LocalDateTime.now())
                .build();

        // mock recent orders larger than threshold
        List<Order> recent = List.of(
                o,
                Order.builder().userId(user).createdAt(LocalDateTime.now()).build(),
                Order.builder().userId(user).createdAt(LocalDateTime.now()).build(),
                Order.builder().userId(user).createdAt(LocalDateTime.now()).build(),
                Order.builder().userId(user).createdAt(LocalDateTime.now()).build(),
                Order.builder().userId(user).createdAt(LocalDateTime.now()).build()
        );
        when(repo.findByUserId(user)).thenReturn(recent);

        double s = scorer.score(o);
        assertEquals(1.0, s, 1e-6, "expected saturated frequency score");
    }

    @Test
    void shouldBlock_hardThreshold() {
        ReflectionTestUtils.setField(scorer, "hardThreshold", 0.5);
        ReflectionTestUtils.setField(scorer, "softThreshold", 0.1);
        ReflectionTestUtils.setField(scorer, "weightAmount", 1.0);
        ReflectionTestUtils.setField(scorer, "weightFrequency", 0.0);

        Order o = Order.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .totalAmount(1000f)
                .createdAt(LocalDateTime.now())
                .build();

        // amount normalizes to tanh(1)=~0.761 > hardThreshold 0.5
        assertTrue(scorer.shouldBlock(o));
    }

    @Test
    void shouldNotBlock_safe() {
        ReflectionTestUtils.setField(scorer, "softThreshold", 0.8);
        ReflectionTestUtils.setField(scorer, "hardThreshold", 0.95);
        ReflectionTestUtils.setField(scorer, "weightAmount", 1.0);
        ReflectionTestUtils.setField(scorer, "weightFrequency", 0.0);

        Order o = Order.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .totalAmount(50f)
                .createdAt(LocalDateTime.now())
                .build();

        assertFalse(scorer.shouldBlock(o));
    }
}
