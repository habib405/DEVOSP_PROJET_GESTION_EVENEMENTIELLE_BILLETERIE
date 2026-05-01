package com.projet.billeterie.back.repository;

import com.projet.billeterie.back.entity.SoldoutPrediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SoldoutPredictionRepository extends JpaRepository<SoldoutPrediction, UUID> {

    List<SoldoutPrediction> findAllByOrderByTimestampAsc();
}
