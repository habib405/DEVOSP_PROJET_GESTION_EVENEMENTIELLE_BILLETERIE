package com.projet.billeterie.back.repository;

import com.projet.billeterie.back.entity.Event;
import com.projet.billeterie.back.entity.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findByOrganizerId(UUID organizerId);
    List<Event> findByStatus(EventStatus status);
}
