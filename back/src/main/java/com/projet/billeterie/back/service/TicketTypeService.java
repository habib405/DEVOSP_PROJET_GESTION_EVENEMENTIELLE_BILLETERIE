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
        TicketType tt = TicketType.builder()
                .event(event)
                .name(request.getName())
                .price(request.getPrice())
                .totalQuantity(request.getTotalQuantity())
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
        tt.setName(request.getName());
        tt.setPrice(request.getPrice());
        tt.setTotalQuantity(request.getTotalQuantity());
        return ticketTypeRepository.save(tt);
    }

    public void delete(UUID id) {
        ticketTypeRepository.delete(findById(id));
    }
}
