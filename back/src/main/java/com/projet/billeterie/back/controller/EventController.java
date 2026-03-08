package com.projet.billeterie.back.controller;

import com.projet.billeterie.back.dto.EventRequest;
import com.projet.billeterie.back.entity.Event;
import com.projet.billeterie.back.entity.User;
import com.projet.billeterie.back.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public List<Event> getPublished() { return eventService.findPublished(); }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public List<Event> getAll() { return eventService.findAll(); }

    @GetMapping("/{id}")
    public Event getById(@PathVariable UUID id) { return eventService.findById(id); }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public ResponseEntity<Event> create(@Valid @RequestBody EventRequest request,
                                        @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.create(request, user.getId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public Event update(@PathVariable UUID id, @Valid @RequestBody EventRequest request) {
        return eventService.update(id, request);
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public Event publish(@PathVariable UUID id) { return eventService.publish(id); }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public Event cancel(@PathVariable UUID id) { return eventService.cancel(id); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
