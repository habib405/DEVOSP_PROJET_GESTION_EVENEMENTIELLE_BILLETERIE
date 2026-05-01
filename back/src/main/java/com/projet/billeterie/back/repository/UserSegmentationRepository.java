package com.projet.billeterie.back.repository;

import com.projet.billeterie.back.entity.UserSegmentation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserSegmentationRepository extends JpaRepository<UserSegmentation, UUID> {

    List<UserSegmentation> findAllByOrderByClusterAsc();
}
