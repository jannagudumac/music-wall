package com.musicwall.repository;

import com.musicwall.entity.AlbumEntity;
import com.musicwall.repository.projection.CatalogSuggestionProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// The repository reads and saves albums. JpaRepository provides the main CRUD operations.
public interface AlbumRepository extends JpaRepository<AlbumEntity, Long> {

    List<AlbumEntity> findAllByOrderByTitleAsc();

    // Find this artist's albums, ordered by release year and then title.
    List<AlbumEntity> findByArtistIdOrderByReleaseYearAscTitleAsc(Long artistId);

    // Search album titles and artist names; PostgreSQL pg_trgm also finds similar spellings.
    @Query(value = """
            SELECT al.*
            FROM album al
            JOIN artist ar ON ar.id = al.artist_id
            WHERE lower(al.title) LIKE '%' || lower(:query) || '%'
               OR lower(ar.name) LIKE '%' || lower(:query) || '%'
               OR lower(al.title) % lower(:query)
               OR lower(ar.name) % lower(:query)
            ORDER BY GREATEST(
                similarity(lower(al.title), lower(:query)),
                similarity(lower(ar.name), lower(:query))
            ) DESC,
            al.title ASC
            """, nativeQuery = true)
    List<AlbumEntity> searchSimilar(@Param("query") String query);

    // Give autocomplete matches a relevance score and return only a limited number.
    @Query(value = """
            SELECT al.id AS id,
                   'ALBUM' AS type,
                   al.title AS title,
                   ar.name AS subtitle,
                   CAST(GREATEST(
                       similarity(lower(al.title), lower(:query)),
                       similarity(lower(ar.name), lower(:query)),
                       CASE
                           WHEN lower(al.title) = lower(:query) THEN 1.50
                           WHEN lower(al.title) LIKE lower(:query) || '%' THEN 1.20
                           WHEN lower(al.title) LIKE '%' || lower(:query) || '%' THEN 1.00
                           ELSE 0.00
                       END
                   ) AS real) AS score
            FROM album al
            JOIN artist ar ON ar.id = al.artist_id
            WHERE lower(al.title) LIKE '%' || lower(:query) || '%'
               OR lower(ar.name) LIKE '%' || lower(:query) || '%'
               OR lower(al.title) % lower(:query)
               OR lower(ar.name) % lower(:query)
            ORDER BY score DESC, al.title ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<CatalogSuggestionProjection> findSuggestions(
            @Param("query") String query,
            @Param("limit") int limit
    );
}
