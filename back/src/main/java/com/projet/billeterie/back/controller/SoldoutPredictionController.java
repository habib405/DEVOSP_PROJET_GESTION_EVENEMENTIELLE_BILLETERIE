package com.projet.billeterie.back.controller;

import com.projet.billeterie.back.entity.SoldoutPrediction;
import com.projet.billeterie.back.service.SoldoutPredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/soldout")
@CrossOrigin("*")
public class SoldoutPredictionController {

    @Autowired
    private SoldoutPredictionService service;

    @GetMapping
    public List<SoldoutPrediction> getSoldoutPredictions() {
        return service.getPredictions();
    }
}
