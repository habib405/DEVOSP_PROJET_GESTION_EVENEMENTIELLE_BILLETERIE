package com.projet.billeterie.back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class VenueRequest {
    @NotBlank private String name;
    @NotBlank private String address;
    @NotBlank private String city;
    @Positive private int maxCapacity;
}
