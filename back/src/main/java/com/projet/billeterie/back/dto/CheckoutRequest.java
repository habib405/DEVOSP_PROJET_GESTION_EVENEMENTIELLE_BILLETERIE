package com.projet.billeterie.back.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CheckoutRequest {
    @NotNull
    private UUID orderId;

    @NotEmpty
    private List<UUID> ticketTypeIds;
}
