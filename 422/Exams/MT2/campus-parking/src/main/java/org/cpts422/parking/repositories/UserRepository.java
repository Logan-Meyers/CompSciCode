package org.cpts422.parking.repositories;

import java.util.Optional;

import org.cpts422.parking.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

/** Looks up users. */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLicensePlate(String licensePlate);
}
