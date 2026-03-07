package com.projet.gestion.evenementielle.repository;

import com.projet.gestion.evenementielle.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VenueRepository extends JpaRepository<Venue, UUID> {
}
