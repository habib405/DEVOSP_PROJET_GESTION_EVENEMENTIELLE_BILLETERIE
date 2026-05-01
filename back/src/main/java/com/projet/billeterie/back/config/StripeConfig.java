package com.projet.billeterie.back.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class StripeConfig {

    @Value("${stripe.api.secret-key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        if (secretKey == null || secretKey.isBlank() || secretKey.contains("REPLACEME")) {
            log.warn("Stripe secret key is not configured — payment endpoints will fail. " +
                    "Set STRIPE_SECRET_KEY env variable (test key from https://dashboard.stripe.com/test/apikeys).");
        }
        Stripe.apiKey = secretKey;
    }
}
