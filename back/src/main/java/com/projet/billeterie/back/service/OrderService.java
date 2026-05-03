package com.projet.billeterie.back.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projet.billeterie.back.dto.OrderRequest;
import com.projet.billeterie.back.entity.Order;
import com.projet.billeterie.back.entity.OrderStatus;
import com.projet.billeterie.back.entity.Registration;
import com.projet.billeterie.back.entity.RegistrationStatus;
import com.projet.billeterie.back.entity.TicketType;
import com.projet.billeterie.back.exception.ResourceNotFoundException;
import com.projet.billeterie.back.repository.EventRepository;
import com.projet.billeterie.back.repository.OrderRepository;
import com.projet.billeterie.back.repository.RegistrationRepository;
import com.projet.billeterie.back.repository.TicketTypeRepository;
import com.projet.billeterie.back.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
        private final RegistrationRepository registrationRepository;
    private final EmailNotificationService emailService;
    private final InvoiceService invoiceService;
    private final FraudMonitoringService fraudMonitoringService;

    // ── 1. CRÉATION ────────────────────────────────────────────────────────
    @Transactional
    public Order create(OrderRequest request, UUID userId) {
        float total = 0.0f;
        
        // 1. On crée d'abord l'entité Order (PENDING) pour avoir son ID
        Order order = Order.builder()
                .userId(userId)
                .totalAmount(0.0f) // Sera mis à jour juste après
                .status(OrderStatus.PENDING)
                .build();
        order = orderRepository.save(order);

        // 2. On boucle sur les billets demandés avec notre nouveau système de verrou !
        for (UUID ttId : request.getTicketTypeIds()) {
            // On utilise findByIdWithLock pour empêcher les accès concurrents
            TicketType tt = ticketTypeRepository.findByIdWithLock(ttId)
                    .orElseThrow(() -> new ResourceNotFoundException("TicketType not found: " + ttId));

            if (!tt.isAvailable()) {
                throw new IllegalStateException("Le billet '" + tt.getName() + "' est épuisé.");
            }

            // 3. ON RÉSERVE IMMÉDIATEMENT LE STOCK
            tt.setSoldQuantity(tt.getSoldQuantity() + 1);
            ticketTypeRepository.save(tt);
            
            // 4. On crée une Registration (Billet) en statut PENDING (sans QR code pour le moment)
            Registration reg = Registration.builder()
                    .orderId(order.getId())
                    .userId(userId)
                    .eventId(tt.getEvent().getId())
                    .ticketType(tt)
                    .status(RegistrationStatus.PENDING) 
                    .build();
            registrationRepository.save(reg);

            total += tt.getPrice();
        }

        // 5. On met à jour le montant final de la commande
        order.setTotalAmount(total);
        order = orderRepository.save(order);

        // 6. Analyse anti-fraude que nous avons mise en place
        fraudMonitoringService.analyzeOrder(order);

        return order;
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
// ── 4. VALIDATION (paiement reçu) ────────────────────────────────────────

    @Transactional
    public Order confirm(UUID orderId) { // <- Plus besoin de OrderRequest !
        Order order = getOrder(orderId);
        order.confirm();
        orderRepository.save(order);

        // On valide les billets PENDING et on génère les QR codes
        List<Registration> registrations = registrationService.confirmRegistrationsForOrder(order);

        // Async email notifications + PDF invoice
        userRepository.findById(order.getUserId()).ifPresent(user -> {
            String fullName = user.getFirstName() + " " + user.getLastName();

            emailService.sendOrderConfirmation(user.getEmail(), fullName, order.getId().toString(), order.getTotalAmount());

            try {
                // On récupère la liste des IDs de billets depuis nos registrations pour la facture
                List<UUID> ticketTypeIds = registrations.stream().map(r -> r.getTicketType().getId()).toList();
                byte[] invoicePdf = invoiceService.generate(order, user, ticketTypeIds);
                
                emailService.sendInvoiceEmail(user.getEmail(), fullName, order.getId().toString(), order.getTotalAmount(), invoicePdf);
            } catch (Exception ex) {
                log.error("Could not generate/send invoice for order {}: {}", order.getId(), ex.getMessage());
            }

            registrations.forEach(reg ->
                    emailService.sendRegistrationConfirmation(
                            user.getEmail(), fullName, reg.getTicketType().getEvent().getTitle(),
                            order.getId().toString(), order.getTotalAmount(), reg.getQrCode()
                    )
            );
        });

        return order;
    }
    // ── ANNULATION ───────────────────────────────────────────────────────────

   @Transactional
    public Order cancel(UUID orderId) {
        Order order = getOrder(orderId);
        
        // On annule tous les billets liés à cette commande (ce qui libère le stock via ton RegistrationService)
        List<Registration> registrations = registrationService.findByOrder(orderId);
        registrations.forEach(reg -> registrationService.cancel(reg.getId()));
        
        // On annule la commande et on sauvegarde
        order.cancel();
        orderRepository.save(order);

        // Envoi de l'email d'annulation
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
