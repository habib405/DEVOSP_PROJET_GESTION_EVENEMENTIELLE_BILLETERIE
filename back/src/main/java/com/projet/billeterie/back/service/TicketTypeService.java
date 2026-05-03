package com.projet.billeterie.back.service;

import com.projet.billeterie.back.dto.TicketTypeRequest;
import com.projet.billeterie.back.entity.Event;
import com.projet.billeterie.back.entity.TicketType;
import com.projet.billeterie.back.exception.ResourceNotFoundException;
import com.projet.billeterie.back.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;
    private final EventService eventService;

    @Transactional
    public TicketType create(TicketTypeRequest request) {
        Event event = eventService.findById(request.getEventId());

        // Sum existing quantities for this event + the new one must stay within event.maxCapacity
        int existingSum = ticketTypeRepository.findByEventId(event.getId()).stream()
                .mapToInt(TicketType::getTotalQuantity)
                .sum();
        int requested = request.getTotalQuantity();
        int projected = existingSum + requested;

        if (projected > event.getMaxCapacity()) {
            throw new IllegalArgumentException(String.format(
                    "Quantité totale demandée (%d existants + %d nouveaux = %d) dépasse la capacité de l'événement (%d).",
                    existingSum, requested, projected, event.getMaxCapacity()));
        }

        TicketType tt = TicketType.builder()
                .event(event)
                .name(request.getName())
                .price(request.getPrice())
                .totalQuantity(requested)
                .soldQuantity(0)
                .build();
        return ticketTypeRepository.save(tt);
    }

    public List<TicketType> findByEvent(UUID eventId) {
        return ticketTypeRepository.findByEventId(eventId);
    }

    public TicketType findById(UUID id) {
        return ticketTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TicketType not found: " + id));
    }

    @Transactional
    public TicketType update(UUID id, TicketTypeRequest request) {
        TicketType tt = findById(id);
        Event event = tt.getEvent();

        int requested = request.getTotalQuantity();

        // Cannot drop the cap below what is already sold
        if (requested < tt.getSoldQuantity()) {
            throw new IllegalArgumentException(String.format(
                    "Quantité demandée (%d) inférieure aux billets déjà vendus (%d).",
                    requested, tt.getSoldQuantity()));
        }

        // Sum of all *other* ticket types for this event + the requested new value must stay within maxCapacity
        int otherSum = ticketTypeRepository.findByEventId(event.getId()).stream()
                .filter(other -> !other.getId().equals(tt.getId()))
                .mapToInt(TicketType::getTotalQuantity)
                .sum();
        int projected = otherSum + requested;

        if (projected > event.getMaxCapacity()) {
            throw new IllegalArgumentException(String.format(
                    "Quantité totale projetée (%d autres + %d = %d) dépasse la capacité de l'événement (%d).",
                    otherSum, requested, projected, event.getMaxCapacity()));
        }

        tt.setName(request.getName());
        tt.setPrice(request.getPrice());
        tt.setTotalQuantity(requested);
        return ticketTypeRepository.save(tt);
    }

    public void delete(UUID id) {
        ticketTypeRepository.delete(findById(id));
    }
}
