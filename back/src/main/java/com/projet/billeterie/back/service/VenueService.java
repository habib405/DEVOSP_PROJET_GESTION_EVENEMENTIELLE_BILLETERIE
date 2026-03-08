package com.projet.billeterie.back.service;

import com.projet.billeterie.back.dto.VenueRequest;
import com.projet.billeterie.back.entity.Venue;
import com.projet.billeterie.back.exception.ResourceNotFoundException;
import com.projet.billeterie.back.repository.VenueRepository;
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
