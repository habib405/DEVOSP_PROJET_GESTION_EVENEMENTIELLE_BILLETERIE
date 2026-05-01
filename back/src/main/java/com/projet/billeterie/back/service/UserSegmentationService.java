package com.projet.billeterie.back.service;

import com.projet.billeterie.back.entity.UserSegmentation;
import com.projet.billeterie.back.repository.UserSegmentationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserSegmentationService {

    @Autowired
    private UserSegmentationRepository repo;

    public List<UserSegmentation> getSegmentation() {
        return repo.findAllByOrderByClusterAsc();
    }
}
