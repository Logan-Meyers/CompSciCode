package org.cpts422.parking.repositories;

import java.util.List;

import org.cpts422.parking.entities.Permit;
import org.springframework.data.jpa.repository.JpaRepository;

/** Finds a user's permits and saves new ones. */
public interface PermitRepository extends JpaRepository<Permit, Long> {

    List<Permit> findByUserId(Long userId);
}
