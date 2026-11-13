
package com.training.gym.validation;

import com.training.gym.dto.trainer.CreateTrainerDto;
import com.training.gym.dto.trainer.UpdateTrainerDto;
import com.training.gym.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class TrainerDtoValidatorTest {

    private static final LocalDate TODAY =
            LocalDate.of(2026, 11, 7);

    private TrainerDtoValidator validator;
    private TrainingType specialization;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-11-07T12:00:00Z"),
                ZoneId.of("Europe/Warsaw")
        );

        validator = new TrainerDtoValidator(clock);
        specialization = new TrainingType(1L, "Strength");
    }

    private CreateTrainerDto validCreateDto() {
        return new CreateTrainerDto(
                "James",
                "Bond",
                LocalDate.of(1990, 5, 15),
                null,
                specialization
        );
    }

    private UpdateTrainerDto validUpdateDto() {
        return new UpdateTrainerDto(
                "James",
                "Bond",
                "james.bond",
                specialization
        );
    }

    @Nested
    class CreateTests {

        @Test
        void shouldAcceptValidCreateDto() {
            assertDoesNotThrow(
                    () -> validator.validateCreate(validCreateDto())
            );
        }

        @Test
        void shouldRejectNullCreateDto() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateCreate(null)
            );

            assertEquals(
                    "CreateTrainerDto must not be null",
                    exception.getMessage()
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   ", "\t", "\n"})
        void shouldRejectInvalidFirstName(String firstName) {
            CreateTrainerDto dto = new CreateTrainerDto(
                    firstName,
                    "Bond",
                    LocalDate.of(1990, 5, 15),
                    null,
                    specialization
            );

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateCreate(dto)
            );

            assertEquals(
                    "First name must not be blank",
                    exception.getMessage()
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   ", "\t", "\n"})
        void shouldRejectInvalidLastName(String lastName) {
            CreateTrainerDto dto = new CreateTrainerDto(
                    "James",
                    lastName,
                    LocalDate.of(1990, 5, 15),
                    null,
                    specialization
            );

            assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateCreate(dto)
            );
        }

        @Test
        void shouldRejectNullSpecialization() {
            CreateTrainerDto dto = new CreateTrainerDto(
                    "James",
                    "Bond",
                    LocalDate.of(1990, 5, 15),
                    null,
                    null
            );

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateCreate(dto)
            );

            assertEquals(
                    "Trainer specialization must not be null",
                    exception.getMessage()
            );
        }


        @Test
        void shouldRejectFutureDateOfBirth() {
            CreateTrainerDto dto = new CreateTrainerDto(
                    "",
                    "Bond",
                    TODAY.plusDays(1),
                    null,
                    specialization
            );

            assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateCreate(dto)
            );
        }

        @Test
        void shouldAcceptDateOfBirthEqualToToday() {
            CreateTrainerDto dto = new CreateTrainerDto(
                    "James", "Bond", TODAY, null, specialization
            );

            assertDoesNotThrow(
                    () -> validator.validateCreate(dto)
            );
        }
    }

    @Nested
    class UpdateTests {

        @Test
        void shouldAcceptValidUpdateDto() {
            assertDoesNotThrow(
                    () -> validator.validateUpdate(validUpdateDto())
            );
        }

        @Test
        void shouldRejectNullUpdateDto() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateUpdate(null)
            );

            assertEquals(
                    "UpdateTrainerDto must not be null",
                    exception.getMessage()
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   ", "\t", "\n"})
        void shouldRejectInvalidUsername(String username) {
            UpdateTrainerDto dto = new UpdateTrainerDto(
                    "James",
                    "Bond",
                    username,
                    specialization
            );

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateUpdate(dto)
            );

            assertEquals(
                    "Username must not be blank",
                    exception.getMessage()
            );
        }

        @Test
        void shouldRejectBlankFirstName() {
            UpdateTrainerDto dto = new UpdateTrainerDto(
                    " ",
                    "Bond",
                    "james.bond",
                    specialization
            );

            assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateUpdate(dto)
            );
        }

        @Test
        void shouldRejectBlankLastName() {
            UpdateTrainerDto dto = new UpdateTrainerDto(
                    "James",
                    " ",
                    "james.bond",
                    specialization
            );

            assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateUpdate(dto)
            );
        }

        @Test
        void shouldRejectNullSpecialization() {
            UpdateTrainerDto dto = new UpdateTrainerDto(
                    "James",
                    "Bond",
                    "james.bond",
                    null
            );

            assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateUpdate(dto)
            );
        }

    }
}
