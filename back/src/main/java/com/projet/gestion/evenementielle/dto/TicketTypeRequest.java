package com.projet.gestion.evenementielle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.util.UUID;

@Data
public class TicketTypeRequest {
    @NotNull  private UUID eventId;
    @NotBlank private String name;
    @NotNull  private Float price;
    @PositiveOrZero private int totalQuantity;
}
