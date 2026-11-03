package org.cpts422.parking.repositories;

import java.util.List;

import org.cpts422.parking.entities.Citation;
import org.springframework.data.jpa.repository.JpaRepository;

/** Finds a user's citations and saves citations. */
public interface CitationRepository extends JpaRepository<Citation, Long> {

    List<Citation> findByUserId(Long userId);
}
