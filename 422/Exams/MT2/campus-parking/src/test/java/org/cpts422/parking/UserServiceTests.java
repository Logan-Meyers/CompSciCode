package org.cpts422.parking;

import org.cpts422.parking.entities.User;
import org.cpts422.parking.entities.UserType;
import org.cpts422.parking.exceptions.UserNotFoundException;
import org.cpts422.parking.repositories.UserRepository;
import org.cpts422.parking.services.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    private static final long USER_ID = 1L;
    private static final String PLATE = "BOB-123";

    @Mock private UserRepository userRepository;

    @InjectMocks private UserService userService;

    /* ---------- R10 - Finding User ---------- */

    @Test
    @DisplayName("R10 - Trying to get a list of users with an empty database should return an empty list")
    void R10_getUsersForEmptyShouldReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        assertTrue(userService.getAllUsers().isEmpty());
    }

    @Test
    @DisplayName("R10 - Trying to get a list of users with a filled database should return a list with the right number and users")
    void R10_getUsersForFullShouldReturnFilledList() {
        // the third argument of User is the license plate, not an email
        List<User> users = List.of(
                new User("Bob", UserType.STUDENT, "BOB-123"),
                new User("Alice", UserType.STUDENT, "ALICE-456"),
                new User("Grayson", UserType.EMPLOYEE, "GRAY-789")
        );
        when(userRepository.findAll()).thenReturn(users);

        // assertEquals on lists already checks the size and every element in order
        assertEquals(users, userService.getAllUsers());
    }

    @Test
    @DisplayName("R10 - Trying to find a user who does exist should return that user")
    void R10_getUserForExistingShouldReturnUser() {
        User user = new User("Bob", UserType.STUDENT, PLATE);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertEquals(user, userService.getUser(USER_ID));
    }

    @Test
    @DisplayName("R10 - Trying to find a user who doesn't exist should error")
    void R10_getUserForMissingShouldError() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUser(USER_ID));
    }

    @Test
    @DisplayName("R10 - Trying to find a user by plate which does exist should return user")
    void R10_getUserByPlateShouldReturnUser() {
        User user = new User("Bob", UserType.STUDENT, PLATE);
        when(userRepository.findByLicensePlate(PLATE)).thenReturn(Optional.of(user));

        assertEquals(user, userService.findByLicensePlate(PLATE));
    }

    @Test
    @DisplayName("R10 - Trying to find a user by plate which doesn't exist should error")
    void R10_getUserByNonExistentPlateShouldError() {
        when(userRepository.findByLicensePlate(PLATE)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findByLicensePlate(PLATE));
    }
}
