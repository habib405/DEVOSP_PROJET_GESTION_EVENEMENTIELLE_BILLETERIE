package com.projet.billeterie.back.controller;

import com.projet.billeterie.back.entity.UserSegmentation;
import com.projet.billeterie.back.service.UserSegmentationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/segmentation")
@CrossOrigin("*")
public class UserSegmentationController {

    @Autowired
    private UserSegmentationService service;

    @GetMapping
    public List<UserSegmentation> getSegmentation() {
        return service.getSegmentation();
    }
}
