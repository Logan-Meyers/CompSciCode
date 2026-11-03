package org.cpts422.parking.services;

import java.util.List;

import org.cpts422.parking.entities.User;
import org.cpts422.parking.exceptions.UserNotFoundException;
import org.cpts422.parking.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reads users for the pages and the officer lookup. */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Returns every user. */
    @Transactional
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /** Returns the user with the given id, or throws {@link UserNotFoundException}. */
    @Transactional
    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: id " + userId));
    }

    /** Returns the user registered to the plate, or throws {@link UserNotFoundException}. */
    @Transactional
    public User findByLicensePlate(String plate) {
        return userRepository.findByLicensePlate(plate)
                .orElseThrow(() -> new UserNotFoundException("No user has license plate " + plate));
    }
}
