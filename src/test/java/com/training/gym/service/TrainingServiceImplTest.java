package com.training.gym.service;

import com.training.gym.dao.TraineeRepository;
import com.training.gym.dao.TrainerRepository;
import com.training.gym.dao.TrainingRepository;
import com.training.gym.dao.TrainingTypeRepository;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.model.Trainee;
import com.training.gym.model.Trainer;
import com.training.gym.model.Training;
import com.training.gym.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {
    @Mock
    TrainingRepository trainings;
    @Mock
    TraineeRepository trainees;
    @Mock
    TrainerRepository trainers;
    @Mock
    TrainingTypeRepository types;
    @InjectMocks
    TrainingServiceImpl service;

    private Training input() {
        return Training.builder().trainee(Trainee.builder().id(1L).build())
                .trainer(Trainer.builder().id(2L).build())
                .trainingType(TrainingType.builder().id(3L).build())
                .trainingName("Fitness session").trainingDate(LocalDate.of(2026, 11, 15))
                .trainingDuration(Duration.ofMinutes(45)).build();
    }

    @Test
    void shouldPersistTrainingWithResolvedReferences() {
        // Arrange
        Training input = input();
        Trainee james = Trainee.builder().id(1L).build();
        Trainer sara = Trainer.builder().id(2L).build();
        TrainingType fitness = TrainingType.builder().id(3L).name("Fitness").build();
        when(trainees.findById(1L)).thenReturn(Optional.of(james));
        when(trainers.findById(2L)).thenReturn(Optional.of(sara));
        when(types.findById(3L)).thenReturn(Optional.of(fitness));
        when(trainings.save(input)).thenReturn(input);
        // Act
        Training result = service.createTraining(input);
        // Assert
        assertSame(input, result);
        assertSame(james, result.getTrainee());
        assertSame(sara, result.getTrainer());
        assertSame(fitness, result.getTrainingType());
        verify(trainings).save(input);
    }

    @Test
    void shouldRejectMissingTrainee() {
        // Arrange
        Training input = input();
        when(trainees.findById(1L)).thenReturn(Optional.empty());
        // Act / Assert
        assertThrows(EntityNotFoundException.class, () -> service.createTraining(input));
        verifyNoInteractions(trainers, types, trainings);
    }

    @Test
    void shouldRejectMissingTrainer() {
        // Arrange
        Training input = input();
        when(trainees.findById(1L)).thenReturn(Optional.of(Trainee.builder().id(1L).build()));
        when(trainers.findById(2L)).thenReturn(Optional.empty());
        // Act / Assert
        assertThrows(EntityNotFoundException.class, () -> service.createTraining(input));
        verifyNoInteractions(types, trainings);
    }

    @Test
    void shouldRejectMissingTrainingType() {
        // Arrange
        Training input = input();
        when(trainees.findById(1L)).thenReturn(Optional.of(Trainee.builder().id(1L).build()));
        when(trainers.findById(2L)).thenReturn(Optional.of(Trainer.builder().id(2L).build()));
        when(types.findById(3L)).thenReturn(Optional.empty());
        // Act / Assert
        assertThrows(EntityNotFoundException.class, () -> service.createTraining(input));
        verify(trainings, never()).save(any());
    }

    @Test
    void shouldFindTrainingById() {
        // Arrange
        Training stored = input();
        when(trainings.findById(7L)).thenReturn(Optional.of(stored));
        // Act
        Training actual = service.getTrainingById(7L);
        // Assert
        assertSame(stored, actual);
    }

    @Test
    void shouldRejectMissingTrainingId() {
        // Arrange
        when(trainings.findById(404L)).thenReturn(Optional.empty());
        // Act / Assert
        assertThrows(EntityNotFoundException.class, () -> service.getTrainingById(404L));
    }
}
