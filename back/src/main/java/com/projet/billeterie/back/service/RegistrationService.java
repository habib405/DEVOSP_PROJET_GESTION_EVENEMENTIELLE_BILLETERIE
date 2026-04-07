package com.projet.billeterie.back.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.projet.billeterie.back.entity.*;
import com.projet.billeterie.back.exception.ResourceNotFoundException;
import com.projet.billeterie.back.repository.EventRepository;
import com.projet.billeterie.back.repository.RegistrationRepository;
import com.projet.billeterie.back.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final EventRepository eventRepository;

    /**
     * Creates one Registration per ticket-type ID and generates its QR code.
     * Called after order is CONFIRMED.
     */
    @Transactional
    public List<Registration> createForOrder(Order order, List<UUID> ticketTypeIds) {
        return ticketTypeIds.stream().map(ttId -> {
            TicketType tt = ticketTypeRepository.findById(ttId)
                    .orElseThrow(() -> new ResourceNotFoundException("TicketType not found: " + ttId));

            String qrContent = "order:" + order.getId() + "|ticket:" + ttId + "|user:" + order.getUserId();
            String qrBase64 = generateQrCodeBase64(qrContent);

            Registration reg = Registration.builder()
                    .orderId(order.getId())
                    .userId(order.getUserId())
                    .eventId(tt.getEvent().getId())
                    .ticketType(tt)
                    .status(RegistrationStatus.CONFIRMED)
                    .qrCode(qrBase64)
                    .build();

            return registrationRepository.save(reg);
        }).toList();
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
