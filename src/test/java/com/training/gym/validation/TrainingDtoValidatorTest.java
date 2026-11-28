package com.training.gym.validation;

import com.training.gym.dto.training.CreateTrainingDto;
import com.training.gym.exception.IncorrectMethodArgumentException;
import com.training.gym.util.validation.TrainingDtoValidator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrainingDtoValidatorTest {
    private final TrainingDtoValidator validator = new TrainingDtoValidator();

    private CreateTrainingDto dto(Long trainee, Long trainer, String name, Long type, LocalDate date, Long duration) {
        return new CreateTrainingDto(trainee, trainer, name, type, date, duration);
    }

    @Test
    void shouldAcceptValidTraining() {
        // Arrange
        CreateTrainingDto input = dto(1L, 2L, "Fitness", 3L, LocalDate.of(2026, 11, 15), 45L);
        // Act / Assert
        assertDoesNotThrow((() -> validator.validateCreate(input)));
    }

    @Test
    void shouldRejectNullDto() {
        // Arrange
        CreateTrainingDto input = null;
        // Act / Assert
        assertThrows(IncorrectMethodArgumentException.class, () -> validator.validateCreate(input));
    }

    @Test
    void shouldRejectInvalidTraineeId() {
        // Arrange
        CreateTrainingDto input = dto(0L, 2L, "Fitness", 3L, LocalDate.now(), 45L);
        // Act / Assert
        assertThrows(IncorrectMethodArgumentException.class, () -> validator.validateCreate(input));
    }

    @Test
    void shouldRejectInvalidTrainerId() {
        // Arrange
        CreateTrainingDto input = dto(1L, -2L, "Fitness", 3L, LocalDate.now(), 45L);
        // Act / Assert
        assertThrows(IncorrectMethodArgumentException.class, () -> validator.validateCreate(input));
    }

    @Test
    void shouldRejectBlankName() {
        // Arrange
        CreateTrainingDto input = dto(1L, 2L, " ", 3L, LocalDate.now(), 45L);
        // Act / Assert
        assertThrows(IncorrectMethodArgumentException.class, () -> validator.validateCreate(input));
    }

    @Test
    void shouldRejectMissingType() {
        // Arrange
        CreateTrainingDto input = dto(1L, 2L, "Fitness", null, LocalDate.now(), 45L);
        // Act / Assert
        assertThrows(IncorrectMethodArgumentException.class, () -> validator.validateCreate(input));
    }

    @Test
    void shouldRejectMissingDate() {
        // Arrange
        CreateTrainingDto input = dto(1L, 2L, "Fitness", 3L, null, 45L);
        // Act / Assert
        assertThrows(IncorrectMethodArgumentException.class, () -> validator.validateCreate(input));
    }

    @Test
    void shouldRejectZeroOrNegativeDuration() {
        // Arrange
        CreateTrainingDto zero = dto(1L, 2L, "Fitness", 3L, LocalDate.now(), 0L);
        CreateTrainingDto negative = dto(1L, 2L, "Fitness", 3L, LocalDate.now(), -1L);
        // Act / Assert
        assertThrows(IncorrectMethodArgumentException.class, () -> validator.validateCreate(zero));
        assertThrows(IncorrectMethodArgumentException.class, () -> validator.validateCreate(negative));
    }
}
