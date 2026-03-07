package com.projet.gestion.evenementielle.service;

import com.projet.gestion.evenementielle.dto.VenueRequest;
import com.projet.gestion.evenementielle.entity.Venue;
import com.projet.gestion.evenementielle.exception.ResourceNotFoundException;
import com.projet.gestion.evenementielle.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;

    public Venue create(VenueRequest request) {
        Venue venue = Venue.builder()
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .maxCapacity(request.getMaxCapacity())
                .build();
        return venueRepository.save(venue);
    }

    public List<Venue> findAll() {
        return venueRepository.findAll();
    }

    public Venue findById(UUID id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found: " + id));
    }

    public Venue update(UUID id, VenueRequest request) {
        Venue venue = findById(id);
        venue.setName(request.getName());
        venue.setAddress(request.getAddress());
        venue.setCity(request.getCity());
        venue.setMaxCapacity(request.getMaxCapacity());
        return venueRepository.save(venue);
    }

    public void delete(UUID id) {
        venueRepository.delete(findById(id));
    }
}
