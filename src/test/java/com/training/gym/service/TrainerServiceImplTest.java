package com.training.gym.service;

import com.training.gym.dao.TraineeRepository;
import com.training.gym.dao.TrainerRepository;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.model.Trainer;
import com.training.gym.util.IdGenerator;
import com.training.gym.util.PasswordGenerator;
import com.training.gym.util.UsernameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {
    @Mock
    TrainerRepository trainerRepository;
    @Mock
    TraineeRepository traineeRepository;
    @Mock
    IdGenerator idGenerator;
    @Mock
    UsernameGenerator usernameGenerator;
    @Mock
    PasswordGenerator passwordGenerator;
    @InjectMocks
    TrainerServiceImpl service;

    private static Trainer trainer() {
        Trainer result = new Trainer();
        result.setFirstName("John");
        result.setLastName("Smith");
        result.setUserName("john.smith");
        return result;
    }

    @Test
    void shouldGenerateCredentialsAndSaveTrainer() {
        // Arrange
        Trainer input = trainer();
        when(idGenerator.generate()).thenReturn(8L);
        when(passwordGenerator.generate()).thenReturn("password123");
        when(usernameGenerator.generate(eq("John"), eq("Smith"), any())).thenReturn("john.smith");
        when(trainerRepository.save(input)).thenReturn(input);
        // Act
        Trainer result = service.createTrainer(input);
        // Assert
        assertSame(input, result);
        assertEquals(8L, result.getId());
        assertEquals("password123", result.getPassword());
        assertTrue(result.isActive());
        verify(trainerRepository).save(input);
    }

    @Test
    void shouldCheckUsernameAcrossBothProfileTypes() {
        // Arrange
        Trainer input = trainer();
        when(usernameGenerator.generate(eq("John"), eq("Smith"), any())).thenAnswer(invocation -> {
            Predicate<String> exists = invocation.getArgument(2);
            assertFalse(exists.test("available"));
            return "available";
        });
        // Act
        service.createTrainer(input);
        // Assert
        verify(traineeRepository).existsByUserName("available");
        verify(trainerRepository).existsByUserName("available");
    }

    @Test
    void shouldRejectNullTrainerOnCreate() {
        // Arrange
        // Act
        assertThrows(NullPointerException.class, () -> service.createTrainer(null));
        // Assert
        verifyNoInteractions(trainerRepository);
    }

    @Test
    void shouldUpdateExistingTrainerPreservingIdAndActive() {
        // Arrange
        Trainer input = trainer();
        Trainer existing = trainer();
        existing.setId(7L);
        existing.setActive(true);
        when(trainerRepository.findByUserName("john.smith")).thenReturn(Optional.of(existing));
        when(trainerRepository.update(input)).thenReturn(input);
        // Act
        Trainer result = service.updateTrainer(input);
        // Assert
        assertSame(input, result);
        assertEquals(7L, result.getId());
        assertTrue(result.isActive());
        verify(trainerRepository).update(input);
    }

    @Test
    void shouldRejectMissingTrainerOnUpdate() {
        // Arrange
        Trainer input = trainer();
        when(trainerRepository.findByUserName("john.smith")).thenReturn(Optional.empty());
        // Act
        assertThrows(EntityNotFoundException.class, () -> service.updateTrainer(input));
        // Assert
        verify(trainerRepository, never()).update(any());
    }

    @Test
    void shouldRejectNullTrainerOnUpdate() {
        // Arrange
        // Act
        assertThrows(NullPointerException.class, () -> service.updateTrainer(null));
        // Assert
        verifyNoInteractions(trainerRepository);
    }

    @Test
    void shouldFindTrainerById() {
        // Arrange
        Trainer expected = trainer();
        when(trainerRepository.findById(4L)).thenReturn(Optional.of(expected));
        // Act
        Trainer result = service.getTrainerById(4L);
        // Assert
        assertSame(expected, result);
    }

    @Test
    void shouldRejectMissingTrainerById() {
        // Arrange
        when(trainerRepository.findById(4L)).thenReturn(Optional.empty());
        // Act
        assertThrows(EntityNotFoundException.class, () -> service.getTrainerById(4L));
        // Assert
        verify(trainerRepository).findById(4L);
    }
}
