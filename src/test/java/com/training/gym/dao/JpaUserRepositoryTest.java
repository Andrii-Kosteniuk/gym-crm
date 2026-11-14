package com.training.gym.dao;

import com.training.gym.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JpaUserRepositoryTest extends RepositoryIntegrationSupport {
    @Autowired
    JpaUserRepository repository;

    @Test
    void shouldFindExistingUserByUsername() {
        // Arrange
        User james = user("James", "Bond", "james.bond");
        em.persist(james);
        reload();
        // Act
        Optional<User> actual = repository.findByUsername("james.bond");
        // Assert
        assertTrue(actual.isPresent());
        assertEquals("James", actual.orElseThrow().getFirstName());
    }

    @Test
    void shouldReturnEmptyForUnknownUsername() {
        // Arrange
        String username = "jym.kerry";
        // Act
        Optional<User> result = repository.findByUsername(username);
        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldDetectUsernameCollision() {
        // Arrange
        em.persist(user("Sara", "Connor", "sara.connor"));
        reload();
        // Act
        boolean exists = repository.existsByUsername("sara.connor");
        // Assert
        assertTrue(exists);
        assertFalse(repository.existsByUsername("jym.kerry"));
    }

    @Test
    void shouldRejectBlankUsername() {
        // Arrange
        String username = " ";
        // Act / Assert
        assertThrows(IllegalArgumentException.class, () -> repository.findByUsername(username));
        assertThrows(IllegalArgumentException.class, () -> repository.existsByUsername(username));
    }

    @Test
    void shouldDeleteManagedUser() {
        // Arrange
        User james = user("James", "Bond", "james.bond");
        em.persist(james);
        Long id = james.getId();
        // Act
        repository.delete(james);
        reload();
        // Assert
        assertNull(em.find(User.class, id));
    }
}
