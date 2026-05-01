package com.projet.billeterie.back.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class FinalizePaymentResponse {
    private UUID orderId;
    private String orderStatus;
    private String paymentStatus;
    private Float totalAmount;
}
