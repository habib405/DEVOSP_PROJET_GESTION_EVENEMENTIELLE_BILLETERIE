package com.projet.gestion.evenementielle.repository;

import com.projet.gestion.evenementielle.entity.Event;
import com.projet.gestion.evenementielle.entity.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findByOrganizerId(UUID organizerId);
    List<Event> findByStatus(EventStatus status);
}
