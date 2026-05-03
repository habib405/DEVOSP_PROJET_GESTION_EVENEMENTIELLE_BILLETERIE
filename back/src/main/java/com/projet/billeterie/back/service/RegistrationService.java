package com.projet.billeterie.back.service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.projet.billeterie.back.entity.Event;
import com.projet.billeterie.back.entity.Order;
import com.projet.billeterie.back.entity.Registration;
import com.projet.billeterie.back.entity.RegistrationStatus;
import com.projet.billeterie.back.entity.TicketType;
import com.projet.billeterie.back.exception.ResourceNotFoundException;
import com.projet.billeterie.back.repository.EventRepository;
import com.projet.billeterie.back.repository.RegistrationRepository;
import com.projet.billeterie.back.repository.TicketTypeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final EventRepository eventRepository;

    /**
     * Valide les Registrations en attente pour une commande confirmée et génère les QR codes.
     */
    @Transactional
    public List<Registration> confirmRegistrationsForOrder(Order order) {
        // On récupère les billets réservés lors de la création de la commande
        List<Registration> registrations = registrationRepository.findByOrderId(order.getId());

        for (Registration reg : registrations) {
            // Génération du QR Code
            String qrContent = "order:" + order.getId() + "|ticket:" + reg.getTicketType().getId() + "|user:" + order.getUserId();
            reg.setQrCode(generateQrCodeBase64(qrContent));
            
            // Passage au statut CONFIRMED
            reg.setStatus(RegistrationStatus.CONFIRMED);

            // C'est seulement maintenant qu'on incrémente les participants de l'événement (le paiement est validé)
            Event event = reg.getTicketType().getEvent();
            event.setCurrentAttendees(event.getCurrentAttendees() + 1);
            eventRepository.save(event);

            registrationRepository.save(reg);
        }
        return registrations;
    }
    public List<Registration> findByUser(UUID userId) {
        return registrationRepository.findByUserId(userId);
    }

    public List<Registration> findByOrder(UUID orderId) {
        return registrationRepository.findByOrderId(orderId);
    }

    public Registration findById(UUID id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found: " + id));
    }

    @Transactional
    public Registration cancel(UUID id) {
        Registration reg = findById(id);

        if (reg.getStatus() == RegistrationStatus.CANCELLED) {
            throw new IllegalStateException("Registration is already cancelled");
        }

        // ── Decrement soldQuantity on TicketType ──────────────────────────────
        TicketType tt = reg.getTicketType();
        if (tt != null && tt.getSoldQuantity() > 0) {
            tt.setSoldQuantity(tt.getSoldQuantity() - 1);
            ticketTypeRepository.save(tt);
        }

        // ── Decrement currentAttendees on Event ───────────────────────────────
        if (reg.getEventId() != null) {
            eventRepository.findById(reg.getEventId()).ifPresent(event -> {
                if (event.getCurrentAttendees() > 0) {
                    event.setCurrentAttendees(event.getCurrentAttendees() - 1);
                    eventRepository.save(event);
                }
            });
        }

        reg.cancel();
        return registrationRepository.save(reg);
    }

    // ── QR Code generation ────────────────────────────────────────────────────

    private String generateQrCodeBase64(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, 200, 200);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            log.error("QR code generation failed: {}", e.getMessage());
            return content; // fallback: store raw content
        }
    }
}
