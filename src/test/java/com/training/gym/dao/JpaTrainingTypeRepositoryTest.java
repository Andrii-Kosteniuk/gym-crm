package com.training.gym.dao;

import com.training.gym.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JpaTrainingTypeRepositoryTest extends RepositoryIntegrationSupport {
    @Autowired
    JpaTrainingTypeRepository repository;

    @Test
    void shouldFindTrainingTypeById() {
        // Arrange
        TrainingType fitness = type("Fitness");
        Long id = fitness.getId();
        reload();
        // Act
        Optional<TrainingType> actual = repository.findById(id);
        // Assert
        assertEquals("Fitness", actual.orElseThrow().getName());
    }

    @Test
    void shouldReturnEmptyForMissingType() {
        // Arrange
        Long id = 999999L;
        // Act
        Optional<TrainingType> actual = repository.findById(id);
        // Assert
        assertTrue(actual.isEmpty());
    }

    @Test
    void shouldSortTypesByName() {
        // Arrange
        type("Yoga");
        type("Fitness");
        reload();
        // Act
        List<TrainingType> actual = repository.findAll();
        // Assert
        List<String> names = actual.stream().map(TrainingType::getName).toList();
        assertEquals(List.of("Fitness", "Yoga"), names);
    }

    @Test
    void shouldRejectNullId() {
        // Arrange
        Long id = null;
        // Act / Assert
        assertThrows(NullPointerException.class, () -> repository.findById(id));
    }
}
