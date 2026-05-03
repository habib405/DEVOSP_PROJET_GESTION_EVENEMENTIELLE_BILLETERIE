package com.projet.billeterie.back.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class EventRequest {
    @NotBlank private String title;
    private String description;
    @NotNull private LocalDateTime startDate;
    @NotNull private LocalDateTime endDate;
    @Positive private int maxCapacity;
    private UUID venueId;
}
