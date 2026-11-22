package com.training.gym.util.validation;

import com.training.gym.dto.trainee.AddressDto;
import com.training.gym.exception.IncorrectMethodArgumentException;

import java.time.Clock;
import java.time.LocalDate;

public final class ValidationUtils {

    public static final String MUST_BE_POSITIVE = "%s must be positive";

    private ValidationUtils() {
    }

    public static <T> void requireNotNull(T value, String fieldName) {
        if (value == null) {
            throw new IncorrectMethodArgumentException("%s must not be null".formatted(fieldName));
        }

    }

    public static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IncorrectMethodArgumentException("%s must not be blank".formatted(fieldName));
        }
    }

    public static void requireValidDateOfBirth(LocalDate date, LocalDate currentDate) {
        requireNotNull(date, "Date of birth");
        requireNotNull(currentDate, "Current date");

        if (date.isAfter(currentDate)) {
            throw new IncorrectMethodArgumentException("Date of birth must not be in the future");
        }
    }

    public static void requirePositiveDuration(Long duration) {
        requireNotNull(duration, "Training duration");

        if (duration == 0 || duration < 0) {
            throw new IncorrectMethodArgumentException("Training duration must be positive");
        }
    }

    public static void requirePositiveId(Long id, String fieldName) {
        requireNotNull(id, fieldName);

        if (id <= 0) {
            throw new IncorrectMethodArgumentException(MUST_BE_POSITIVE.formatted(fieldName));
        }
    }

    public static void requirePositiveValue(int number, String fieldName) {

        if (number <= 0) {
            throw new IncorrectMethodArgumentException(MUST_BE_POSITIVE.formatted(fieldName));
        }
    }

    public static void validDateOfBirth(LocalDate date, String fieldName) {
        requireNotNull(date, "Date of birth");

        if (date.equals(LocalDate.now(Clock.systemDefaultZone()))) {
            throw new IncorrectMethodArgumentException("%s must not be current day".formatted(fieldName));
        }

        if (date.isAfter(LocalDate.now(Clock.systemDefaultZone()))) {
            throw new IncorrectMethodArgumentException("%s must not be in the future".formatted(fieldName));
        }
    }

    public static void validateAddress(AddressDto address, String fieldName) {
        requireNotNull(address, "%s must not be null".formatted(fieldName));
        requireNotNull(address.city(), "%s must not be null".formatted(fieldName));
        requirePositiveValue(address.buildingNumber(), "%s building number must be positive".formatted(fieldName));
        requireNotBlank(address.zipCode(), MUST_BE_POSITIVE.formatted(fieldName));
    }
}