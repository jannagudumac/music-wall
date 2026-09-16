package com.musicwall.repository;

import com.musicwall.entity.MusicWallEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// The repository reads and saves walls using Spring Data JPA.
public interface MusicWallRepository extends JpaRepository<MusicWallEntity, Long> {

    // Find owned or shared walls, newest first; Distinct prevents duplicate walls in the results.
    List<MusicWallEntity> findDistinctByOwnerUsernameOrMembersUsernameOrderByIdDesc(
            String ownerUsername,
            String memberUsername
    );
}
