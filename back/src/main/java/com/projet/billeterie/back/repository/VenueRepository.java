package com.projet.billeterie.back.repository;

import com.projet.billeterie.back.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VenueRepository extends JpaRepository<Venue, UUID> {
}
