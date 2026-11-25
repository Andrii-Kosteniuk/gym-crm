package com.training.gym.util;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DurationMinutesConverterTest {
    private final DurationMinutesConverter converter = new DurationMinutesConverter();

    @Test
    void shouldStoreMinutesAsNumericValue() {
        // Arrange
        Duration duration = Duration.ofMinutes(45);
        // Act
        Long result = converter.convertToDatabaseColumn(duration);
        // Assert
        assertEquals(45L, result);
    }

    @Test
    void shouldRestoreDurationFromMinutes() {
        // Arrange
        Long stored = 60L;
        // Act
        Duration result = converter.convertToEntityAttribute(stored);
        // Assert
        assertEquals(Duration.ofHours(1), result);
    }
}
