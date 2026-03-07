package com.projet.gestion.evenementielle.controller;

import com.projet.gestion.evenementielle.dto.OrderRequest;
import com.projet.gestion.evenementielle.entity.Order;
import com.projet.gestion.evenementielle.entity.User;
import com.projet.gestion.evenementielle.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Cycle de vie d'une commande :
 *   POST   /api/orders              → Création   (PENDING)
 *   PATCH  /api/orders/{id}/lock    → Verrouillage (LOCKED)
 *   PATCH  /api/orders/{id}/pay     → Paiement   (PAYMENT_PENDING)
 *   PATCH  /api/orders/{id}/confirm → Validation  (CONFIRMED) + email + QR
 *   PATCH  /api/orders/{id}/cancel  → Annulation
 *   PATCH  /api/orders/{id}/refund  → Remboursement
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> create(@Valid @RequestBody OrderRequest request,
                                        @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.create(request, user.getId()));
    }

    @PatchMapping("/{id}/lock")
    public Order lock(@PathVariable UUID id) {
        return orderService.lock(id);
    }

    @PatchMapping("/{id}/pay")
    public Order pay(@PathVariable UUID id) {
        return orderService.initiatePayment(id);
    }

    @PatchMapping("/{id}/confirm")
    public Order confirm(@PathVariable UUID id, @Valid @RequestBody OrderRequest request) {
        return orderService.confirm(id, request);
    }

    @PatchMapping("/{id}/cancel")
    public Order cancel(@PathVariable UUID id) {
        return orderService.cancel(id);
    }

    @PatchMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public Order refund(@PathVariable UUID id) {
        return orderService.refund(id);
    }

    @GetMapping("/{id}")
    public Order getById(@PathVariable UUID id) { return orderService.getOrder(id); }

    @GetMapping("/my")
    public List<Order> myOrders(@AuthenticationPrincipal User user) {
        return orderService.findByUser(user.getId());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Order> getAll() { return orderService.findAll(); }
}
