package com.projet.billeterie.back.controller;

import com.projet.billeterie.back.dto.CheckoutRequest;
import com.projet.billeterie.back.dto.CheckoutResponse;
import com.projet.billeterie.back.dto.FinalizePaymentResponse;
import com.projet.billeterie.back.dto.OrderRequest;
import com.projet.billeterie.back.entity.Order;
import com.projet.billeterie.back.entity.OrderStatus;
import com.projet.billeterie.back.entity.User;
import com.projet.billeterie.back.exception.ResourceNotFoundException;
import com.projet.billeterie.back.repository.UserRepository;
import com.projet.billeterie.back.service.OrderService;
import com.projet.billeterie.back.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Stripe Checkout (sandbox) flow.
 *
 *   POST /api/payments/checkout
 *     → moves the order to LOCKED + PAYMENT_PENDING and returns the Stripe-hosted URL.
 *
 *   POST /api/payments/finalize?session_id=...
 *     → called after Stripe redirects to the success URL.
 *     → verifies payment_status == "paid", confirms the order
 *       (creates registrations, generates QR codes, sends email + invoice PDF).
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final StripeService stripeService;
    private final OrderService orderService;
    private final UserRepository userRepository;
    private final com.projet.billeterie.back.service.FraudScorer fraudScorer;

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> createCheckout(@Valid @RequestBody CheckoutRequest req,
                                                           @AuthenticationPrincipal User user) throws StripeException {
        Order order = orderService.getOrder(req.getOrderId());

        if (!order.getUserId().equals(user.getId())) {
            throw new IllegalStateException("Order does not belong to current user.");
        }

        // Move state PENDING → LOCKED → PAYMENT_PENDING (idempotent on repeated calls)
        if (order.getStatus() == OrderStatus.PENDING) {
            order = orderService.lock(order.getId());
        }
        if (order.getStatus() == OrderStatus.LOCKED) {
            order = orderService.initiatePayment(order.getId());
        }

        // Run fraud scoring before creating the external checkout session.
        double fraudScore = fraudScorer.score(order);
        if (fraudScorer.shouldBlock(order)) {
            // release reserved tickets
            orderService.cancel(order.getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Payment blocked by fraud check (score=" + String.format("%.3f", fraudScore) + ")");
        }

        Session session = stripeService.createCheckoutSession(order, req.getTicketTypeIds(), user.getEmail());
        return ResponseEntity.ok(new CheckoutResponse(session.getId(), session.getUrl()));
    }

    @PostMapping("/finalize")
    public ResponseEntity<FinalizePaymentResponse> finalizePayment(@RequestParam("session_id") String sessionId,
                                                                   @AuthenticationPrincipal User user) throws StripeException {
        Session session = stripeService.retrieveSession(sessionId);

        // Ownership: the session metadata orderId must match an order owned by this user
        String orderIdStr = session.getMetadata().get("orderId");
        String ttIdsStr = session.getMetadata().get("ticketTypeIds");
        if (orderIdStr == null || ttIdsStr == null) {
            throw new ResourceNotFoundException("Stripe session is missing order metadata.");
        }

        UUID orderId = UUID.fromString(orderIdStr);
        Order order = orderService.getOrder(orderId);
        if (!order.getUserId().equals(user.getId())) {
            throw new IllegalStateException("Order does not belong to current user.");
        }

        String paymentStatus = session.getPaymentStatus(); // "paid", "unpaid", "no_payment_required"

        if ("paid".equalsIgnoreCase(paymentStatus)) {
            // Idempotency: only confirm if not already confirmed
            if (order.getStatus() != OrderStatus.CONFIRMED) {
                List<UUID> ticketTypeIds = Arrays.stream(ttIdsStr.split(","))
                        .filter(s -> !s.isBlank())
                        .map(UUID::fromString)
                        .collect(Collectors.toList());

                OrderRequest req = new OrderRequest();
                req.setTicketTypeIds(ticketTypeIds);
                order = orderService.confirm(orderId, req);
                log.info("Order {} confirmed via Stripe session {}", orderId, sessionId);
            }
        } else {
            log.warn("Stripe session {} reports payment_status={}, order remains {}",
                    sessionId, paymentStatus, order.getStatus());
        }

        return ResponseEntity.ok(new FinalizePaymentResponse(
                order.getId(),
                order.getStatus().name(),
                paymentStatus,
                order.getTotalAmount()
        ));
    }
}
