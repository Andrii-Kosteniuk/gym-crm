package com.training.gym.service;

import com.training.gym.domain.model.*;
import com.training.gym.exception.BusinessRuleViolationException;
import com.training.gym.exception.IncorrectMethodArgumentException;
import com.training.gym.exception.UserNotFoundException;
import com.training.gym.persistence.entity.TraineeEntity;
import com.training.gym.persistence.entity.TrainerEntity;
import com.training.gym.persistence.entity.TrainingEntity;
import com.training.gym.persistence.entity.TrainingTypeEntity;
import com.training.gym.repository.TraineeRepository;
import com.training.gym.repository.TrainerRepository;
import com.training.gym.repository.TrainingRepository;
import com.training.gym.repository.TrainingTypeRepository;
import com.training.gym.service.mapper.TraineePersistenceMapper;
import com.training.gym.service.mapper.TrainerPersistenceMapper;
import com.training.gym.service.mapper.TrainingPersistenceMapper;
import com.training.gym.service.mapper.TrainingTypePersistenceMapper;
import com.training.gym.service.training.TrainingServiceImpl;
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
    TrainingRepository trainingRepository;
    @Mock
    TraineeRepository traineeRepository;
    @Mock
    TrainerRepository trainerRepository;
    @Mock
    TrainingTypeRepository trainingTypeRepository;
    @Mock
    TrainingPersistenceMapper trainingMapper;
    @Mock
    TraineePersistenceMapper traineeMapper;
    @Mock
    TrainerPersistenceMapper trainerMapper;
    @Mock
    TrainingTypePersistenceMapper trainingTypeMapper;
    @InjectMocks
    TrainingServiceImpl service;

    private Training training() {
        return Training.builder()
                .trainee(Trainee.builder().id(1L).build())
                .trainer(Trainer.builder().id(2L).build())
                .trainingType(TrainingType.builder().id(3L).build())
                .trainingName("Session")
                .trainingDate(LocalDate.now().plusDays(2))
                .trainingDuration(Duration.ofMinutes(45))
                .build();
    }

    private Trainee trainee(boolean active) {
        return Trainee.builder().id(1L)
                .user(User.builder().active(active).build()).build();
    }

    private Trainer trainer(boolean active, Long specializationId) {
        return Trainer.builder().id(2L)
                .user(User.builder().active(active).build())
                .specialization(TrainingType.builder().id(specializationId).build())
                .build();
    }

    private void stubParticipants(Trainee trainee, Trainer trainer) {
        var traineeEntity = new TraineeEntity();
        var trainerEntity = new TrainerEntity();
        var typeEntity = new TrainingTypeEntity();
        typeEntity.setId(3L);
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(traineeEntity));
        when(trainerRepository.findById(2L)).thenReturn(Optional.of(trainerEntity));
        when(trainingTypeRepository.findById(3L)).thenReturn(Optional.of(typeEntity));
        when(traineeMapper.toTraineeDomain(traineeEntity)).thenReturn(trainee);
        when(trainerMapper.toTrainerDomain(trainerEntity)).thenReturn(trainer);
        when(trainingTypeMapper.toTrainingTypeDomain(typeEntity))
                .thenReturn(TrainingType.builder().id(3L).build());
    }

    @Test
    void createsTraining() {
        // Arrange
        var input = training();
        stubParticipants(trainee(true), trainer(true, 3L));
        var entity = new TrainingEntity();
        var saved = Training.builder().id(10L).build();
        when(trainingMapper.toTrainingEntity(input)).thenReturn(entity);
        when(trainingRepository.save(entity)).thenReturn(entity);
        when(trainingMapper.toTrainingDomain(entity)).thenReturn(saved);

        // Act
        var result = service.createTraining(input);

        // Assert
        assertSame(saved, result);
        verify(trainingRepository).save(entity);
    }

    @Test
    void rejectsNullTraining() {
        // Arrange
        // Act & Assert
        assertThrows(NullPointerException.class, () -> service.createTraining(null));
        verifyNoInteractions(trainingRepository);
    }

    @Test
    void rejectsTrainingWithId() {
        // Arrange
        var input = training();
        input.setId(9L);

        // Act & Assert
        assertThrows(IncorrectMethodArgumentException.class, () -> service.createTraining(input));

        verifyNoInteractions(trainingRepository);
    }

    @Test
    void rejectsMissingTraineeReference() {
        // Arrange
        var input = training();
        input.setTrainee(null);

        // Act & Assert
        assertThrows(IncorrectMethodArgumentException.class, () -> service.createTraining(input));

        verifyNoInteractions(trainingRepository);
    }

    @Test
    void rejectsInvalidTrainerId() {
        // Arrange
        var input = training();
        input.setTrainer(Trainer.builder().id(-1L).build());

        // Act & Assert
        assertThrows(IncorrectMethodArgumentException.class, () -> service.createTraining(input));

        verifyNoInteractions(trainingRepository);
    }

    @Test
    void rejectsMissingTrainingTypeId() {
        // Arrange
        var input = training();
        input.setTrainingType(TrainingType.builder().build());

        // Act & Assert
        assertThrows(IncorrectMethodArgumentException.class, () -> service.createTraining(input));

        verifyNoInteractions(trainingRepository);
    }


    @Test
    void rejectsMissingTrainee() {
        // Arrange
        var input = training();

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> service.createTraining(input));

        verify(trainingRepository, never()).save(any());
    }

    @Test
    void rejectsMissingTrainer() {
        // Arrange
        var input = training();
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(new TraineeEntity()));

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> service.createTraining(input));

        verify(trainingRepository, never()).save(any());
    }

    @Test
    void rejectsMissingTrainingType() {
        // Arrange
        var input = training();
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(new TraineeEntity()));
        when(trainerRepository.findById(2L)).thenReturn(Optional.of(new TrainerEntity()));

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> service.createTraining(input));

        verify(trainingRepository, never()).save(any());
    }

    @Test
    void rejectsInactiveTrainee() {
        // Arrange
        var input = training();
        stubParticipants(trainee(false), trainer(true, 3L));

        // Act & Assert
        assertThrows(BusinessRuleViolationException.class, () -> service.createTraining(input));

        verify(trainingRepository, never()).save(any());
    }

    @Test
    void rejectsInactiveTrainer() {
        // Arrange
        var input = training();
        stubParticipants(trainee(true), trainer(false, 3L));

        // Act & Assert
        assertThrows(BusinessRuleViolationException.class, () -> service.createTraining(input));

        verify(trainingRepository, never()).save(any());
    }

    @Test
    void rejectsSpecializationMismatch() {
        // Arrange
        var input = training();
        stubParticipants(trainee(true), trainer(true, 9L));

        // Act & Assert
        assertThrows(BusinessRuleViolationException.class, () -> service.createTraining(input));

        verify(trainingRepository, never()).save(any());
    }

    @Test
    void findsTrainingById() {
        // Arrange
        var entity = new TrainingEntity();
        var training = training();
        when(trainingRepository.findById(10L)).thenReturn(Optional.of(entity));
        when(trainingMapper.toTrainingDomain(entity)).thenReturn(training);

        // Act
        var result = service.getTrainingById(10L);

        // Assert
        assertSame(training, result);
    }

    @Test
    void rejectsMissingTraining() {
        // Arrange
        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> service.getTrainingById(99L));
    }
}
