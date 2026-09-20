package com.musicwall.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.musicwall.entity.GenreEntity;

// The repository handles genre data. JpaRepository provides the main CRUD operations.
public interface GenreRepository extends JpaRepository<GenreEntity, Long> {

    List<GenreEntity> findAllByOrderByNameAsc();

    // Search genre names containing the entered text, ignoring letter case, 
    // then sort alphabetically.
    List<GenreEntity> findByNameContainingIgnoreCaseOrderByNameAsc(String query);

    // Find the genres matching the supplied ids.
    List<GenreEntity> findByIdIn(Collection<Long> ids);
}
