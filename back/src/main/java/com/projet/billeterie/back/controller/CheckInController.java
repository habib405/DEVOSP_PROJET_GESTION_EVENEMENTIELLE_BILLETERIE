package com.projet.billeterie.back.controller;

import com.projet.billeterie.back.dto.CheckInRequest;
import com.projet.billeterie.back.entity.CheckIn;
import com.projet.billeterie.back.entity.User;
import com.projet.billeterie.back.service.CheckInService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/checkins")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    /**
     * Staff scans a QR code — creates a CheckIn record and returns validity.
     */
    @PostMapping
    public ResponseEntity<CheckIn> process(@Valid @RequestBody CheckInRequest request,
                                           @AuthenticationPrincipal User staff) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(checkInService.process(request, staff.getId()));
    }

    @GetMapping("/registration/{registrationId}")
    public List<CheckIn> byRegistration(@PathVariable UUID registrationId) {
        return checkInService.findByRegistration(registrationId);
    }

    @GetMapping("/{id}")
    public CheckIn getById(@PathVariable UUID id) {
        return checkInService.findById(id);
    }
}
