package com.projet.gestion.evenementielle.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CheckInRequest {
    @NotNull private UUID registrationId;
}
