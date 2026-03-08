package com.projet.billeterie.back.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromAddress;

    @Async
    public void sendOrderConfirmation(String toEmail, String userName, String orderId, Float totalAmount) {
        log.info(">>> Tentative d'envoi email de confirmation à {}", toEmail);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("Confirmation de votre commande - EventPlatform");
            message.setText(String.format("""
                    Bonjour %s,

                    Votre commande a été confirmée avec succès !

                    Détails :
                      - ID Commande   : %s
                      - Montant total : %.2f €

                    Vos billets ont été générés. Vous pouvez les consulter dans votre espace personnel.

                    Merci pour votre achat,
                    L'équipe EventPlatform
                    """, userName, orderId, totalAmount));
            mailSender.send(message);
            log.info("Confirmation email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send confirmation email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    @Async
    public void sendOrderCancellation(String toEmail, String userName, String orderId) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("Annulation de votre commande - EventPlatform");
            message.setText(String.format("""
                    Bonjour %s,

                    Votre commande %s a été annulée.

                    Si vous pensez qu'il s'agit d'une erreur, veuillez nous contacter.

                    Cordialement,
                    L'équipe EventPlatform
                    """, userName, orderId));
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send cancellation email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    @Async
    public void sendRegistrationConfirmation(String toEmail, String userName, String eventTitle,
                                              String orderId, Float totalAmount, String qrCodeBase64) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Votre billet pour " + eventTitle + " - EventPlatform");

            String html = String.format("""
                    <html><body style="font-family:Arial,sans-serif;">
                    <h2>Bonjour %s,</h2>
                    <p>Votre inscription à l'événement <strong>%s</strong> est confirmée !</p>
                    <p><strong>Détails :</strong></p>
                    <ul>
                      <li>ID Commande &nbsp;&nbsp;: %s</li>
                      <li>Montant total : %.2f €</li>
                    </ul>
                    <p>Voici votre QR code d'entrée. Présentez-le à l'accueil de l'événement :</p>
                    <p><img src="cid:qrcode" alt="QR Code" width="200" height="200" /></p>
                    <p>À bientôt,<br/>L'équipe EventPlatform</p>
                    </body></html>
                    """, userName, eventTitle, orderId, totalAmount);

            helper.setText(html, true);

            byte[] qrBytes = Base64.getDecoder().decode(qrCodeBase64);
            helper.addInline("qrcode", new ByteArrayResource(qrBytes), "image/png");

            mailSender.send(mimeMessage);
            log.info("Registration email with QR code sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send registration email to {}: {}", toEmail, e.getMessage(), e);
        }
    }
}
