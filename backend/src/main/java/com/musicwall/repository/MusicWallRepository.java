package com.musicwall.repository;

import com.musicwall.entity.MusicWallEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MusicWallRepository extends JpaRepository<MusicWallEntity, Long> {

    List<MusicWallEntity> findDistinctByOwnerUsernameOrMembersUsernameOrderByIdDesc(
            String ownerUsername,
            String memberUsername
    );
}
