package com.training.gym.util;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordGeneratorImplTest {
    private final PasswordGeneratorImpl generator = new PasswordGeneratorImpl();

    @Test
    void shouldGenerateTenAlphanumericCharacters() {
        // Arrange
        int samples = 100;
        // Act / Assert
        for (int i = 0; i < samples; i++) assertTrue(generator.generate().matches("[A-Za-z0-9]{10}"));
    }

    @Test
    void shouldGenerateMoreThanOneDistinctPassword() {
        // Arrange
        Set<String> generated = new HashSet<>();
        // Act
        for (int i = 0; i < 100; i++) generated.add(generator.generate());
        // Assert
        assertTrue(generated.size() > 1);
    }
}
