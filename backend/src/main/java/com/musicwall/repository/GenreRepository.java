package com.musicwall.repository;

import com.musicwall.entity.GenreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface GenreRepository extends JpaRepository<GenreEntity, Long> {

    List<GenreEntity> findAllByOrderByNameAsc();

    List<GenreEntity> findByNameContainingIgnoreCaseOrderByNameAsc(String query);

    List<GenreEntity> findByIdIn(Collection<Long> ids);
}
