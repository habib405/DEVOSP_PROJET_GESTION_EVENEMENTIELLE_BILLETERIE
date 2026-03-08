package com.projet.billeterie.back.service;

import com.projet.billeterie.back.dto.EventRequest;
import com.projet.billeterie.back.entity.Event;
import com.projet.billeterie.back.entity.EventStatus;
import com.projet.billeterie.back.entity.Venue;
import com.projet.billeterie.back.exception.ResourceNotFoundException;
import com.projet.billeterie.back.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final VenueService venueService;

    @Transactional
    public Event create(EventRequest request, UUID organizerId) {
        Venue venue = venueService.findById(request.getVenueId());
        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .maxCapacity(request.getMaxCapacity())
                .currentAttendees(0)
                .status(EventStatus.DRAFT)
                .organizerId(organizerId)
                .venue(venue)
                .build();
        return eventRepository.save(event);
    }

    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    public List<Event> findPublished() {
        return eventRepository.findByStatus(EventStatus.PUBLISHED);
    }

    public Event findById(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + id));
    }

    @Transactional
    public Event update(UUID id, EventRequest request) {
        Event event = findById(id);
        Venue venue = venueService.findById(request.getVenueId());
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setMaxCapacity(request.getMaxCapacity());
        event.setVenue(venue);
        return eventRepository.save(event);
    }

    @Transactional
    public Event publish(UUID id) {
        Event event = findById(id);
        event.publish();
        return eventRepository.save(event);
    }

    @Transactional
    public Event cancel(UUID id) {
        Event event = findById(id);
        event.cancel();
        return eventRepository.save(event);
    }

    public void delete(UUID id) {
        eventRepository.delete(findById(id));
    }
}
