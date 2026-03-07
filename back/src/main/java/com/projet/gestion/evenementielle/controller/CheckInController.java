package com.projet.gestion.evenementielle.controller;

import com.projet.gestion.evenementielle.dto.CheckInRequest;
import com.projet.gestion.evenementielle.entity.CheckIn;
import com.projet.gestion.evenementielle.entity.User;
import com.projet.gestion.evenementielle.service.CheckInService;
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

    /** Le staff scanne un QR code — crée un CheckIn et retourne la validité. */
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
    public CheckIn getById(@PathVariable UUID id) { return checkInService.findById(id); }
}
