package com.projet.billeterie.back.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class OrderRequest {
    /** List of ticket-type IDs the user wants to purchase (one registration per entry). */
    @NotEmpty
    private List<UUID> ticketTypeIds;
}
