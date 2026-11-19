package com.training.gym.validation;

import com.training.gym.dto.trainee.CreateTraineeDto;
import com.training.gym.dto.trainee.UpdateTraineeDto;
import com.training.gym.util.validation.TraineeDtoValidator;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TraineeDtoValidatorTest {
    private final TraineeDtoValidator validator = new TraineeDtoValidator(Clock.fixed(Instant.parse("2026-11-14T00:00:00Z"), ZoneOffset.UTC));

    private CreateTraineeDto create(String first, String last, LocalDate dob) {
        return new CreateTraineeDto(first, last, dob, null);
    }

    @Test
    void shouldAcceptValidCreate() {
        // Arrange
        CreateTraineeDto dto = create("James", "Bond", LocalDate.of(1990, 4, 13));
        // Act / Assert
        assertDoesNotThrow(() -> validator.validateCreate(dto));
    }

    @Test
    void shouldRejectNullCreate() {
        // Arrange
        CreateTraineeDto dto = null;
        // Act / Assert
        assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(dto));
    }

    @Test
    void shouldRejectBlankNames() {
        // Arrange
        LocalDate dob = LocalDate.of(1990, 4, 13);
        // Act / Assert
        assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(create(" ", "Bond", dob)));
        assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(create("James", " ", dob)));
    }

    @Test
    void shouldRejectFutureDateOfBirth() {
        // Arrange
        CreateTraineeDto dto = create("James", "Bond", LocalDate.of(2026, 11, 15));
        // Act / Assert
        assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(dto));
    }

    @Test
    void shouldRejectMissingUpdateUsername() {
        // Arrange
        UpdateTraineeDto dto = new UpdateTraineeDto(1L, "James", "Bond", " ", null, null);
        // Act / Assert
        assertThrows(IllegalArgumentException.class, () -> validator.validateUpdate(dto));
    }

    @Test
    void shouldAcceptValidUpdate() {
        // Arrange
        UpdateTraineeDto dto = new UpdateTraineeDto(2L,"Jym", "Kerry", "james.bond", null, null);
        // Act / Assert
        assertDoesNotThrow(() -> validator.validateUpdate(dto));
    }
}
