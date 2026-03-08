package com.projet.billeterie.back.service;

import com.projet.billeterie.back.dto.CheckInRequest;
import com.projet.billeterie.back.entity.CheckIn;
import com.projet.billeterie.back.entity.Registration;
import com.projet.billeterie.back.exception.ResourceNotFoundException;
import com.projet.billeterie.back.repository.CheckInRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final RegistrationService registrationService;

    @Transactional
    public CheckIn process(CheckInRequest request, UUID staffId) {
        Registration registration = registrationService.findById(request.getRegistrationId());

        // Prevent duplicate valid check-ins for the same registration
        boolean alreadyCheckedIn = checkInRepository
                .findByRegistrationId(registration.getId())
                .stream()
                .anyMatch(CheckIn::getIsValid);
        if (alreadyCheckedIn) {
            throw new IllegalStateException("This ticket has already been scanned and checked in.");
        }

        CheckIn checkIn = CheckIn.builder()
                .registration(registration)
                .staffId(staffId)
                .build();

        checkIn.process();
        return checkInRepository.save(checkIn);
    }

    public List<CheckIn> findByRegistration(UUID registrationId) {
        return checkInRepository.findByRegistrationId(registrationId);
    }

    public CheckIn findById(UUID id) {
        return checkInRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CheckIn not found: " + id));
    }
}
