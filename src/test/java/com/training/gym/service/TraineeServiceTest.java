package com.training.gym.service;

import com.training.gym.dao.TraineeRepository;
import com.training.gym.dao.TrainerRepository;
import com.training.gym.dao.TrainingRepository;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.exception.ResourceConflictException;
import com.training.gym.model.Trainee;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {
    @Mock
    TraineeRepository traineeRepository;
    @Mock
    TrainerRepository trainerRepository;
    @Mock
    TrainingRepository trainingRepository;
    @Mock
    IdGenerator idGenerator;
    @Mock
    UsernameGenerator usernameGenerator;
    @Mock
    PasswordGenerator passwordGenerator;
    @InjectMocks
    TraineeServiceImpl service;

    private static Trainee trainee() {
        Trainee trainee = new Trainee();
        trainee.setFirstName("Jane");
        trainee.setLastName("Doe");
        trainee.setUserName("jane.doe");
        return trainee;
    }

    @Test
    void shouldAssignGeneratedCredentialsAndSaveTrainee() {
        // Arrange
        Trainee input = trainee();
        when(idGenerator.generate()).thenReturn(5L);
        when(passwordGenerator.generate()).thenReturn("password123");
        when(usernameGenerator.generate(eq("Jane"), eq("Doe"), any())).thenReturn("jane.doe");
        when(traineeRepository.save(input)).thenReturn(input);
        // Act
        Trainee result = service.createTrainee(input);
        // Assert
        assertSame(input, result);
        assertEquals(5L, result.getId());
        assertEquals("password123", result.getPassword());
        assertEquals("jane.doe", result.getUserName());
        assertTrue(result.isActive());
        verify(traineeRepository).save(input);
    }

    @Test
    void shouldCheckBothRepositoriesForUsernameUniqueness() {
        // Arrange
        Trainee input = trainee();
        when(usernameGenerator.generate(eq("Jane"), eq("Doe"), any())).thenAnswer(invocation -> {
            Predicate<String> exists = invocation.getArgument(2);
            assertFalse(exists.test("available"));
            return "available";
        });
        // Act
        service.createTrainee(input);
        // Assert
        verify(traineeRepository).existsByUserName("available");
        verify(trainerRepository).existsByUserName("available");
    }

    @Test
    void shouldUpdateExistingTraineePreservingIdAndActiveStatus() {
        // Arrange
        Trainee input = trainee();
        Trainee existing = trainee();
        existing.setId(9L);
        existing.setActive(true);
        when(traineeRepository.findByUserName("jane.doe")).thenReturn(Optional.of(existing));
        when(traineeRepository.update(input)).thenReturn(input);
        // Act
        Trainee result = service.updateTrainee(input);
        // Assert
        assertSame(input, result);
        assertEquals(9L, result.getId());
        assertTrue(result.isActive());
        verify(traineeRepository).update(input);
    }

    @Test
    void shouldRejectUpdateWhenTraineeDoesNotExist() {
        // Arrange
        Trainee input = trainee();
        when(traineeRepository.findByUserName("jane.doe")).thenReturn(Optional.empty());
        // Act
        assertThrows(EntityNotFoundException.class, () -> service.updateTrainee(input));
        // Assert
        verify(traineeRepository, never()).update(any());
    }

    @Test
    void shouldReturnExistingTraineeById() {
        // Arrange
        Trainee expected = trainee();
        when(traineeRepository.findById(3L)).thenReturn(Optional.of(expected));
        // Act
        Trainee result = service.getTraineeById(3L);
        // Assert
        assertSame(expected, result);
    }

    @Test
    void shouldRejectMissingTraineeById() {
        // Arrange
        when(traineeRepository.findById(3L)).thenReturn(Optional.empty());
        // Act
        assertThrows(EntityNotFoundException.class, () -> service.getTraineeById(3L));
        // Assert
        verify(traineeRepository).findById(3L);
    }

    @Test
    void shouldRejectNullId() {
        // Arrange
        // Act
        assertThrows(NullPointerException.class, () -> service.getTraineeById(null));
        // Assert
        verifyNoInteractions(traineeRepository);
    }

    @Test
    void shouldDeleteTraineeWithoutTrainings() {
        // Arrange
        when(traineeRepository.existsById(3L)).thenReturn(true);
        // Act
        service.removeTrainee(3L);
        // Assert
        verify(trainingRepository).existsByTraineeId(3L);
        verify(traineeRepository).deleteById(3L);
    }

    @Test
    void shouldRejectDeleteWhenTraineeDoesNotExist() {
        // Arrange
        when(traineeRepository.existsById(3L)).thenReturn(false);
        // Act
        assertThrows(EntityNotFoundException.class, () -> service.removeTrainee(3L));
        // Assert
        verify(traineeRepository, never()).deleteById(anyLong());
    }

    @Test
    void shouldRejectDeleteWhenTrainingsExist() {
        // Arrange
        when(traineeRepository.existsById(3L)).thenReturn(true);
        when(trainingRepository.existsByTraineeId(3L)).thenReturn(true);
        // Act
        assertThrows(ResourceConflictException.class, () -> service.removeTrainee(3L));
        // Assert
        verify(traineeRepository, never()).deleteById(anyLong());
    }
}
