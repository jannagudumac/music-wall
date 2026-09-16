package com.musicwall.repository;

import com.musicwall.entity.MusicSectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// The repository reads and saves wall sections using Spring Data JPA.
public interface MusicSectionRepository extends JpaRepository<MusicSectionEntity, Long> {

    // Read a wall's sections in a stable order.
    List<MusicSectionEntity> findByWallIdOrderByIdAsc(Long wallId);

    // Remove the sections after the service has deleted their items.
    void deleteByWallId(Long wallId);
}
