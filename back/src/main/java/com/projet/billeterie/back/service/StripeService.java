package com.projet.billeterie.back.service;

import com.projet.billeterie.back.entity.Event;
import com.projet.billeterie.back.entity.Order;
import com.projet.billeterie.back.entity.TicketType;
import com.projet.billeterie.back.exception.ResourceNotFoundException;
import com.projet.billeterie.back.repository.TicketTypeRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Wraps Stripe Checkout Session creation and retrieval (test/sandbox mode).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StripeService {

    private final TicketTypeRepository ticketTypeRepository;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    @Value("${stripe.currency:eur}")
    private String currency;

    /**
     * Creates a hosted Checkout Session for an order with the given ticket types.
     * Each ticket-type ID = one line item (qty 1). Identical IDs are grouped automatically.
     */
    public Session createCheckoutSession(Order order, List<UUID> ticketTypeIds, String customerEmail) throws StripeException {
        // Group ticketTypeIds → quantities
        Map<UUID, Long> grouped = ticketTypeIds.stream()
                .collect(Collectors.groupingBy(id -> id, Collectors.counting()));

        SessionCreateParams.Builder builder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .setClientReferenceId(order.getId().toString())
                .setCustomerEmail(customerEmail);

        for (Map.Entry<UUID, Long> entry : grouped.entrySet()) {
            TicketType tt = ticketTypeRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("TicketType not found: " + entry.getKey()));
            Event ev = tt.getEvent();

            long unitAmountCents = Math.round(tt.getPrice() * 100.0);

            SessionCreateParams.LineItem.PriceData.ProductData productData =
                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName(ev.getTitle() + " — " + tt.getName())
                            .setDescription("Billet pour " + ev.getTitle())
                            .build();

            SessionCreateParams.LineItem.PriceData priceData =
                    SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(currency)
                            .setUnitAmount(unitAmountCents)
                            .setProductData(productData)
                            .build();

            SessionCreateParams.LineItem item = SessionCreateParams.LineItem.builder()
                    .setQuantity(entry.getValue())
                    .setPriceData(priceData)
                    .build();

            builder.addLineItem(item);
        }

        // Metadata: store order id + ticket-type ids so the success endpoint can finalize the order
        Map<String, String> meta = new HashMap<>();
        meta.put("orderId", order.getId().toString());
        meta.put("ticketTypeIds", ticketTypeIds.stream().map(UUID::toString).collect(Collectors.joining(",")));
        builder.putAllMetadata(meta);

        Session session = Session.create(builder.build());
        log.info("Created Stripe Checkout session {} for order {}", session.getId(), order.getId());
        return session;
    }

    public Session retrieveSession(String sessionId) throws StripeException {
        return Session.retrieve(sessionId);
    }
}
