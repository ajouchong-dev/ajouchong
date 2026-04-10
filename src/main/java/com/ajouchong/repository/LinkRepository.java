package com.ajouchong.repository;

import com.ajouchong.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {
    
    List<Link> findAllByOrderByCreatedAtAsc();

    @Query("SELECT l FROM Link l WHERE l.active = true OR l.active IS NULL ORDER BY l.createdAt ASC")
    List<Link> findVisibleLinksOrderByCreatedAtAsc();
    
    @Query(value = "SELECT MAX(id) FROM links", nativeQuery = true)
    Optional<Long> findMaxId();
    
    @Query(value = "SELECT pg_get_serial_sequence('links', 'id')", nativeQuery = true)
    Optional<String> getSequenceName();
}