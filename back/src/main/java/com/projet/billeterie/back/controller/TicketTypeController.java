package com.projet.billeterie.back.controller;

import com.projet.billeterie.back.dto.TicketTypeRequest;
import com.projet.billeterie.back.entity.TicketType;
import com.projet.billeterie.back.service.TicketTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ticket-types")
@RequiredArgsConstructor
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    @GetMapping("/event/{eventId}")
    public List<TicketType> getByEvent(@PathVariable UUID eventId) {
        return ticketTypeService.findByEvent(eventId);
    }

    @GetMapping("/{id}")
    public TicketType getById(@PathVariable UUID id) { return ticketTypeService.findById(id); }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public ResponseEntity<TicketType> create(@Valid @RequestBody TicketTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketTypeService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public TicketType update(@PathVariable UUID id, @Valid @RequestBody TicketTypeRequest request) {
        return ticketTypeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        ticketTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
