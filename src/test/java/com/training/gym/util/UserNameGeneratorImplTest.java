package com.training.gym.util;

import com.training.gym.exception.IncorrectMethodArgumentException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserNameGeneratorImplTest {
    private final UserNameGeneratorImpl generator = new UserNameGeneratorImpl();

    @Test
    void shouldNormalizeNames() {
        // Arrange
        String first = " James ", last = " Bond ";
        // Act
        String result = generator.generate(first, last, name -> false);
        // Assert
        assertEquals("james.bond", result);
    }

    @Test
    void shouldAppendSuffixWhenUsernameExists() {
        // Arrange
        Set<String> existing = Set.of("sara.connor", "sara.connor1", "sara.connor2");
        // Act
        String result = generator.generate("Sara", "Connor", existing::contains);
        // Assert
        assertEquals("sara.connor3", result);
    }

    @Test
    void shouldRejectNullOrBlankNames() {
        // Arrange
        String[] invalid = {null, "", "  "};
        // Act / Assert
        for (String value : invalid) {
            assertThrows(IncorrectMethodArgumentException.class, () -> generator.generate(value, "Bond", name -> false));
            assertThrows(IncorrectMethodArgumentException.class, () -> generator.generate("James", value, name -> false));
        }
    }

    @Test
    void shouldRejectMissingExistencePredicate() {
        // Arrange
        java.util.function.Predicate<String> predicate = null;
        // Act / Assert
        assertThrows(NullPointerException.class, () -> generator.generate("Jym", "Kerry", predicate));
    }
}
