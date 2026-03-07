package com.projet.gestion.evenementielle.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.projet.gestion.evenementielle.entity.*;
import com.projet.gestion.evenementielle.exception.ResourceNotFoundException;
import com.projet.gestion.evenementielle.repository.RegistrationRepository;
import com.projet.gestion.evenementielle.repository.TicketTypeRepository;
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

    /**
     * Crée une Registration par ticket-type ID avec son QR code.
     * Appelé après confirmation de la commande.
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
        reg.cancel();
        return registrationRepository.save(reg);
    }

    // ── Génération QR Code ─────────────────────────────────────────────────

    private String generateQrCodeBase64(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, 200, 200);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            log.error("QR code generation failed: {}", e.getMessage());
            return content; // fallback : stocker le contenu brut
        }
    }
}
