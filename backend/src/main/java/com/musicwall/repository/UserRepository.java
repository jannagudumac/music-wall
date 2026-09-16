package com.musicwall.repository;

import com.musicwall.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// The repository handles account data. JpaRepository provides the main CRUD operations.
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // Spring Data builds the username lookup; Optional allows for an account that does not exist.
    Optional<UserEntity> findByUsername(String username);

    // Check whether the username is taken before creating an account.
    boolean existsByUsername(String username);

    // JPQL searches entity fields rather than SQL tables; find matching usernames and exclude the
    // owner.
    @Query("""
            select user from UserEntity user
            where lower(user.username) like lower(concat('%', :query, '%'))
              and user.username <> :currentUsername
            order by user.username
            """)
    List<UserEntity> searchByUsername(
            @Param("query") String query,
            @Param("currentUsername") String currentUsername
    );
}
