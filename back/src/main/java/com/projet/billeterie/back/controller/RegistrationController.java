package com.projet.billeterie.back.controller;

import com.projet.billeterie.back.entity.Registration;
import com.projet.billeterie.back.entity.User;
import com.projet.billeterie.back.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @GetMapping("/my")
    public List<Registration> myRegistrations(@AuthenticationPrincipal User user) {
        return registrationService.findByUser(user.getId());
    }

    @GetMapping("/order/{orderId}")
    public List<Registration> byOrder(@PathVariable UUID orderId) {
        return registrationService.findByOrder(orderId);
    }

    @GetMapping("/{id}")
    public Registration getById(@PathVariable UUID id) {
        return registrationService.findById(id);
    }

    @PatchMapping("/{id}/cancel")
    public Registration cancel(@PathVariable UUID id) {
        return registrationService.cancel(id);
    }
}
