package com.projet.billeterie.back.service;

import com.projet.billeterie.back.dto.OrderRequest;
import com.projet.billeterie.back.entity.*;
import com.projet.billeterie.back.exception.ResourceNotFoundException;
import com.projet.billeterie.back.repository.EventRepository;
import com.projet.billeterie.back.repository.OrderRepository;
import com.projet.billeterie.back.repository.TicketTypeRepository;
import com.projet.billeterie.back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Manages the full order lifecycle:
 *   PENDING → LOCKED → PAYMENT_PENDING → CONFIRMED
 *   At any non-CONFIRMED step: → CANCELLED
 *   CONFIRMED → REFUNDED
 *
 * On CONFIRMED: creates Registrations with QR codes and fires async SMTP notifications.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RegistrationService registrationService;
    private final EmailNotificationService emailService;

    // ── 1. CRÉATION ──────────────────────────────────────────────────────────

    @Transactional
    public Order create(OrderRequest request, UUID userId) {
        List<TicketType> ticketTypes = request.getTicketTypeIds().stream()
                .map(ttId -> ticketTypeRepository.findById(ttId)
                        .orElseThrow(() -> new ResourceNotFoundException("TicketType not found: " + ttId)))
                .toList();

        for (TicketType tt : ticketTypes) {
            if (!tt.isAvailable()) {
                throw new IllegalStateException("TicketType '" + tt.getName() + "' is sold out.");
            }
        }

        float total = (float) ticketTypes.stream().mapToDouble(TicketType::getPrice).sum();

        Order order = Order.builder()
                .userId(userId)
                .totalAmount(total)
                .status(OrderStatus.PENDING)
                .build();

        return orderRepository.save(order);
    }

    // ── 2. VERROUILLAGE ──────────────────────────────────────────────────────

    @Transactional
    public Order lock(UUID orderId) {
        Order order = getOrder(orderId);
        order.lock();
        return orderRepository.save(order);
    }

    // ── 3. PAIEMENT ──────────────────────────────────────────────────────────

    @Transactional
    public Order initiatePayment(UUID orderId) {
        Order order = getOrder(orderId);
        order.initPayment();
        return orderRepository.save(order);
    }

    // ── 4. VALIDATION (paiement reçu) ────────────────────────────────────────

    @Transactional
    public Order confirm(UUID orderId, OrderRequest originalRequest) {
        Order order = getOrder(orderId);
        order.confirm();
        orderRepository.save(order);

        // Create registrations with QR codes
        List<Registration> registrations = registrationService.createForOrder(order, originalRequest.getTicketTypeIds());

        // Update sold quantities and attendee counts
        originalRequest.getTicketTypeIds().forEach(ttId -> {
            TicketType tt = ticketTypeRepository.findById(ttId)
                    .orElseThrow(() -> new ResourceNotFoundException("TicketType not found: " + ttId));
            tt.setSoldQuantity(tt.getSoldQuantity() + 1);
            ticketTypeRepository.save(tt);

            Event event = tt.getEvent();
            event.setCurrentAttendees(event.getCurrentAttendees() + 1);
            eventRepository.save(event);
        });

        // Async email notifications
        userRepository.findById(order.getUserId()).ifPresent(user -> {
            String fullName = user.getFirstName() + " " + user.getLastName();

            emailService.sendOrderConfirmation(
                    user.getEmail(),
                    fullName,
                    order.getId().toString(),
                    order.getTotalAmount()
            );

            registrations.forEach(reg ->
                    emailService.sendRegistrationConfirmation(
                            user.getEmail(),
                            fullName,
                            reg.getTicketType().getEvent().getTitle(),
                            order.getId().toString(),
                            order.getTotalAmount(),
                            reg.getQrCode()
                    )
            );
        });

        return order;
    }

    // ── ANNULATION ───────────────────────────────────────────────────────────

    @Transactional
    public Order cancel(UUID orderId) {
        Order order = getOrder(orderId);
        order.cancel();
        orderRepository.save(order);

        userRepository.findById(order.getUserId()).ifPresent(user ->
                emailService.sendOrderCancellation(
                        user.getEmail(),
                        user.getFirstName() + " " + user.getLastName(),
                        order.getId().toString()
                )
        );
        return order;
    }

    // ── REMBOURSEMENT ─────────────────────────────────────────────────────────

    @Transactional
    public Order refund(UUID orderId) {
        Order order = getOrder(orderId);
        order.refund();
        return orderRepository.save(order);
    }

    // ── QUERIES ───────────────────────────────────────────────────────────────

    public Order getOrder(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    public List<Order> findByUser(UUID userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }
}
