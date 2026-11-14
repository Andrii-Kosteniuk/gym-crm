package com.training.gym.validation;

import com.training.gym.dto.trainer.CreateTrainerDto;
import com.training.gym.dto.trainer.UpdateTrainerDto;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrainerDtoValidatorTest {
    private final TrainerDtoValidator validator = new TrainerDtoValidator(Clock.systemUTC());

    @Test
    void shouldAcceptValidRegistration() {
        // Arrange
        CreateTrainerDto dto = new CreateTrainerDto("Sara", "Connor", 1L);
        // Act / Assert
        assertDoesNotThrow(() -> validator.validateCreate(dto));
    }

    @Test
    void shouldRejectNullRegistration() {
        // Arrange
        CreateTrainerDto dto = null;
        // Act / Assert
        assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(dto));
    }

    @Test
    void shouldRejectBlankFirstOrLastName() {
        // Arrange
        CreateTrainerDto missingFirst = new CreateTrainerDto(" ", "Connor", 1L);
        CreateTrainerDto missingLast = new CreateTrainerDto("Sara", " ", 1L);
        // Act / Assert
        assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(missingFirst));
        assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(missingLast));
    }

    @Test
    void shouldRejectBlankUpdateUsername() {
        // Arrange
        UpdateTrainerDto dto = new UpdateTrainerDto("Sara", "Connor", " ", 1L);
        // Act / Assert
        assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(dto));
    }

    @Test
    void shouldAcceptValidUpdate() {
        // Arrange
        UpdateTrainerDto dto = new UpdateTrainerDto("Sara", "Connor", "sara.connor", 1L);
        // Act / Assert
        assertDoesNotThrow(() -> validator.validateUpdate(dto));
    }
}
