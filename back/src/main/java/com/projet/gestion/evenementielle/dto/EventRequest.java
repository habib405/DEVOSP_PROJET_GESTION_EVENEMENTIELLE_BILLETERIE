package com.projet.gestion.evenementielle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class EventRequest {
    @NotBlank private String title;
    private String description;
    @NotNull private LocalDateTime startDate;
    @NotNull private LocalDateTime endDate;
    @Positive private int maxCapacity;
    @NotNull private UUID venueId;
}
