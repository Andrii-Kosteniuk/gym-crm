package com.training.gym.validation;

import com.training.gym.dto.trainee.CreateTraineeDto;
import com.training.gym.dto.trainee.UpdateTraineeDto;
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

class TraineeDtoValidatorTest {

    private static final LocalDate TODAY =
            LocalDate.of(2026, 11, 7);

    private TraineeDtoValidator validator;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-11-07T12:00:00Z"),
                ZoneId.of("Europe/Warsaw")
        );

        validator = new TraineeDtoValidator(clock);
    }

    private CreateTraineeDto validCreateDto() {
        return new CreateTraineeDto(
                "James",
                "Bond",
                LocalDate.of(2000, 5, 15),
                null
        );
    }

    private UpdateTraineeDto validUpdateDto() {
        return new UpdateTraineeDto(
                "James",
                "Bond",
                "james.bond",
                LocalDate.of(2000, 5, 15),
                null
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
                    "CreateTraineeDto must not be null",
                    exception.getMessage()
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   ", "\t", "\n"})
        void shouldRejectNullOrBlankFirstName(String firstName) {
            CreateTraineeDto dto = new CreateTraineeDto(
                    firstName,
                    "Bond",
                    LocalDate.of(2000, 5, 15),
                    null
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
        void shouldRejectNullOrBlankLastName(String lastName) {
            CreateTraineeDto dto = new CreateTraineeDto(
                    "James",
                    lastName,
                    LocalDate.of(2000, 5, 15),
                    null
            );

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateCreate(dto)
            );

            assertEquals(
                    "Last name must not be blank",
                    exception.getMessage()
            );
        }

        @Test
        void shouldRejectNullDateOfBirth() {
            CreateTraineeDto dto = new CreateTraineeDto(
                    "James", "Bond", null, null
            );

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateCreate(dto)
            );

            assertEquals(
                    "Date of birth must not be null",
                    exception.getMessage()
            );
        }

        @Test
        void shouldRejectFutureDateOfBirth() {
            CreateTraineeDto dto = new CreateTraineeDto(
                    "James",
                    "Bond",
                    TODAY.plusDays(1),
                    null
            );

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateCreate(dto)
            );

            assertEquals(
                    "Date of birth must not be in the future",
                    exception.getMessage()
            );
        }

        @Test
        void shouldAcceptDateOfBirthEqualToToday() {
            CreateTraineeDto dto = new CreateTraineeDto(
                    "James", "Bond", TODAY, null
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
                    "UpdateTraineeDto must not be null",
                    exception.getMessage()
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   ", "\t", "\n"})
        void shouldRejectNullOrBlankUsername(String username) {
            UpdateTraineeDto dto = new UpdateTraineeDto(
                    "James",
                    "Bond",
                    username,
                    LocalDate.of(2000, 5, 15),
                    null
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
            UpdateTraineeDto dto = new UpdateTraineeDto(
                    " ",
                    "Bond",
                    "james.bond",
                    LocalDate.of(2000, 5, 15),
                    null
            );

            assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateUpdate(dto)
            );
        }

        @Test
        void shouldRejectBlankLastName() {
            UpdateTraineeDto dto = new UpdateTraineeDto(
                    "James",
                    " ",
                    "james.bond",
                    LocalDate.of(2000, 5, 15),
                    null
            );

            assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateUpdate(dto)
            );
        }

        @Test
        void shouldRejectFutureDateOfBirth() {
            UpdateTraineeDto dto = new UpdateTraineeDto(
                    "James",
                    "Bond",
                    "james.bond",
                    TODAY.plusDays(1),
                    null
            );

            assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateUpdate(dto)
            );
        }

        @Test
        void shouldRejectNullDateOfBirth() {
            UpdateTraineeDto dto = new UpdateTraineeDto(
                    "James", "Bond", "james.bond", null, null
            );

            assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateUpdate(dto)
            );
        }
    }
}