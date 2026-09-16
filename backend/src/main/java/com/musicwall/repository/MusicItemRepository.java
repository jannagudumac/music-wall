package com.musicwall.repository;

import com.musicwall.entity.MusicItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// The repository reads and saves section items using Spring Data JPA.
public interface MusicItemRepository extends JpaRepository<MusicItemEntity, Long> {

    // Read a section's items in a stable order.
    List<MusicItemEntity> findBySectionIdOrderByIdAsc(Long sectionId);

    // Remove the items before their parent section is deleted.
    void deleteBySectionId(Long sectionId);
}
