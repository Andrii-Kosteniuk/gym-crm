package com.training.gym.service;

import com.training.gym.dao.TraineeRepository;
import com.training.gym.dao.TrainerRepository;
import com.training.gym.dao.TrainingRepository;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.model.Training;
import com.training.gym.util.IdGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {
    @Mock
    TrainingRepository trainingRepository;
    @Mock
    TraineeRepository traineeRepository;
    @Mock
    TrainerRepository trainerRepository;
    @Mock
    IdGenerator idGenerator;
    @InjectMocks
    TrainingServiceImpl service;

    @Test
    void shouldSaveTrainingAfterValidatingReferences() {
        // Arrange
        Training training = new Training();
        training.setId(1L);
        training.setTraineeId(2L);
        training.setTrainerId(3L);
        when(traineeRepository.existsById(2L)).thenReturn(true);
        when(trainerRepository.existsById(3L)).thenReturn(true);
        when(idGenerator.generate()).thenReturn(10L);
        when(trainingRepository.save(training)).thenReturn(training);
        // Act
        Training result = service.createTraining(training);
        // Assert
        assertSame(training, result);
        assertEquals(10L, result.getId());
        verify(trainingRepository).save(training);
    }

    @Test
    void shouldRejectNullTraining() {
        // Arrange: null training
        // Act
        assertThrows(NullPointerException.class, () -> service.createTraining(null));
        // Assert
        verifyNoInteractions(trainingRepository);
    }

    @Test
    void shouldRejectMissingTraineeReference() {
        // Arrange
        Training training = new Training();
        training.setTraineeId(2L);
        training.setTrainerId(3L);
        // Act
        assertThrows(EntityNotFoundException.class, () -> service.createTraining(training));
        // Assert
        verifyNoInteractions(trainingRepository);
        verifyNoInteractions(trainerRepository);
    }

    @Test
    void shouldRejectMissingTrainerReference() {
        // Arrange
        Training training = new Training();
        training.setTraineeId(2L);
        training.setTrainerId(3L);
        when(traineeRepository.existsById(2L)).thenReturn(true);
        // Act
        assertThrows(EntityNotFoundException.class, () -> service.createTraining(training));
        // Assert
        verifyNoInteractions(trainingRepository);
    }

    @Test
    void shouldFindTrainingById() {
        // Arrange
        Training training = new Training();
        when(trainingRepository.findById(4L)).thenReturn(Optional.of(training));
        // Act
        Training result = service.getTrainingById(4L);
        // Assert
        assertSame(training, result);
    }

    @Test
    void shouldRejectMissingTrainingById() {
        // Arrange
        when(trainingRepository.findById(4L)).thenReturn(Optional.empty());
        // Act
        assertThrows(EntityNotFoundException.class, () -> service.getTrainingById(4L));
        // Assert
        verify(trainingRepository).findById(4L);
    }
}
