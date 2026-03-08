package com.projet.billeterie.back.controller;

import com.projet.billeterie.back.dto.VenueRequest;
import com.projet.billeterie.back.entity.Venue;
import com.projet.billeterie.back.service.VenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    @GetMapping
    public List<Venue> getAll() { return venueService.findAll(); }

    @GetMapping("/{id}")
    public Venue getById(@PathVariable UUID id) { return venueService.findById(id); }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public ResponseEntity<Venue> create(@Valid @RequestBody VenueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(venueService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public Venue update(@PathVariable UUID id, @Valid @RequestBody VenueRequest request) {
        return venueService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        venueService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
