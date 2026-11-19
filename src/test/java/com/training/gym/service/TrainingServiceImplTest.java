package com.training.gym.service;

import com.training.gym.domain.model.*;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.repository.TraineeRepository;
import com.training.gym.repository.TrainerRepository;
import com.training.gym.repository.TrainingRepository;
import com.training.gym.repository.TrainingTypeRepository;
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

    private Training training() {
        return Training.builder()
                .trainee(Trainee.builder().id(1L).build())
                .trainer(Trainer.builder().id(2L).build())
                .trainingType(TrainingType.builder()
                        .id(3L)
                        .build())
                .trainingName("Fitness session").trainingDate(LocalDate.now().plusDays(1))
                .trainingDuration(Duration.ofMinutes(45)).build();
    }

    @Test
    void shouldPersistTrainingWithResolvedReferences() {
        // Arrange
        Training training = training();

        TrainingType fitness = TrainingType.builder()
                .id(3L)
                .name("Fitness")
                .build();

        User user = User.builder()
                .active(true)
                .build();

        Trainee james = Trainee.builder()
                .id(1L)
                .user(user)
                .build();

        Trainer sara = Trainer.builder()
                .id(2L)
                .user(user)
                .specialization(fitness)
                .build();



        when(trainees.findById(1L)).thenReturn(Optional.of(james));
        when(trainers.findById(2L)).thenReturn(Optional.of(sara));
        when(types.findById(3L)).thenReturn(Optional.of(fitness));

        when(trainings.save(training)).thenReturn(training);
        // Act
        Training result = service.createTraining(training);
        // Assert
        assertSame(training, result);
        assertSame(james, result.getTrainee());
        assertSame(sara, result.getTrainer());
        assertSame(fitness, result.getTrainingType());
        verify(trainings).save(training);
    }

    @Test
    void shouldRejectMissingTrainee() {
        // Arrange
        Training input = training();
        when(trainees.findById(1L)).thenReturn(Optional.empty());
        // Act / Assert
        assertThrows(EntityNotFoundException.class, () -> service.createTraining(input));
        verifyNoInteractions(trainers, types, trainings);
    }

    @Test
    void shouldRejectMissingTrainer() {
        // Arrange
        Training input = training();
        when(trainees.findById(1L)).thenReturn(Optional.of(Trainee.builder().id(1L).build()));
        when(trainers.findById(2L)).thenReturn(Optional.empty());
        // Act / Assert
        assertThrows(EntityNotFoundException.class, () -> service.createTraining(input));
        verifyNoInteractions(types, trainings);
    }

    @Test
    void shouldRejectMissingTrainingType() {
        // Arrange
        Training input = training();
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
        Training stored = training();
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
