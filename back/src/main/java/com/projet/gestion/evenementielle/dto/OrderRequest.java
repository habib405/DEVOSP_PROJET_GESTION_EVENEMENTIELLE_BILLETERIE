package com.projet.gestion.evenementielle.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class OrderRequest {
    /** Liste des ticket-type IDs à acheter (une inscription par entrée). */
    @NotEmpty
    private List<UUID> ticketTypeIds;
}
