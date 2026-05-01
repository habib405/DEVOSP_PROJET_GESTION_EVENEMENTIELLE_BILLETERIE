package com.projet.billeterie.back.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckoutResponse {
    private String sessionId;
    private String checkoutUrl;
}
