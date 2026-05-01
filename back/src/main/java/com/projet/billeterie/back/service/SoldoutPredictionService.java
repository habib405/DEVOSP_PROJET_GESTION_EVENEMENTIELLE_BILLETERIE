package com.projet.billeterie.back.service;

import com.projet.billeterie.back.entity.SoldoutPrediction;
import com.projet.billeterie.back.repository.SoldoutPredictionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SoldoutPredictionService {

    @Autowired
    private SoldoutPredictionRepository repo;

    public List<SoldoutPrediction> getPredictions() {
        return repo.findAllByOrderByTimestampAsc();
    }
}
