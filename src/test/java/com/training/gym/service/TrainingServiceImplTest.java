package com.training.gym.service;

import com.training.gym.domain.model.*;
import com.training.gym.exception.BusinessRuleViolationException;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.exception.IncorrectMethodArgumentException;
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
        return Training.builder().trainee(Trainee.builder().id(1L).build())
                .trainer(Trainer.builder().id(2L).build())
                .trainingType(TrainingType.builder().id(3L).build())
                .trainingName("Session").trainingDate(LocalDate.now().plusDays(2))
                .trainingDuration(Duration.ofMinutes(45)).build();
    }

    private Trainee activeTrainee() {
        return Trainee.builder()
                .id(1L)
                .user(User.builder()
                        .active(true)
                        .build())
                .build();
    }

    private Trainer activeTrainer(Long specializationId) {
        return Trainer.builder().id(2L).user(User.builder().active(true).build())
                .specialization(TrainingType.builder().id(specializationId).build()).build();
    }

    private void stubParticipants(Training input, Trainee t, Trainer r) {
        TraineeEntity traineeEntity = new TraineeEntity();
        TrainerEntity trainerEntity = new TrainerEntity();
        TrainingTypeEntity typeEntity = new TrainingTypeEntity();
        typeEntity.setId(3L);
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(traineeEntity));
        when(trainerRepository.findById(2L)).thenReturn(Optional.of(trainerEntity));
        when(trainingTypeRepository.findById(3L)).thenReturn(Optional.of(typeEntity));
        when(traineeMapper.toTraineeDomain(traineeEntity)).thenReturn(t);
        when(trainerMapper.toTrainerDomain(trainerEntity)).thenReturn(r);
        when(trainingTypeMapper.toTrainingTypeDomain(typeEntity)).thenReturn(TrainingType.builder().id(3L).build());
    }

    @Test
    void createResolvesJpaEntitiesAndMapsSavedTraining() {
        Training training = training();
        stubParticipants(training, activeTrainee(), activeTrainer(3L));
        TrainingEntity trainingEntity = new TrainingEntity();
        trainingEntity.setId(10L);
        Training result = Training.builder().id(10L).build();
        when(trainingMapper.toTrainingEntity(training)).thenReturn(trainingEntity);
        when(trainingRepository.save(trainingEntity)).thenReturn(trainingEntity);
        when(trainingMapper.toTrainingDomain(trainingEntity)).thenReturn(result);
        assertSame(result, service.createTraining(training));
        verify(trainingRepository).save(trainingEntity);
    }

    @Test
    void invalidReferencesNeverReachRepository() {
        assertThrows(NullPointerException.class, () -> service.createTraining(null));
        Training training = training();
        training.setId(9L);
        Training finalTraining = training;
        assertThrows(IncorrectMethodArgumentException.class, () -> service.createTraining(finalTraining));
        training = training();
        training.setTrainee(null);
        Training finalValue = training;
        Training finalValue1 = finalValue;
        assertThrows(IncorrectMethodArgumentException.class, () -> service.createTraining(finalValue1));
        training = training();
        training.setTrainer(Trainer.builder().id(-1L).build());
        finalValue = training;
        Training finalValue2 = finalValue;
        assertThrows(IncorrectMethodArgumentException.class, () -> service.createTraining(finalValue2));
        training = training();
        training.setTrainingType(TrainingType.builder().build());
        finalValue = training;
        Training finalValue3 = finalValue;
        assertThrows(IncorrectMethodArgumentException.class, () -> service.createTraining(finalValue3));
        training = training();
        training = Training.builder().trainee(training.getTrainee()).trainer(training.getTrainer())
                .trainingType(training.getTrainingType()).trainingName("Session")
                .trainingDate(LocalDate.now().minusDays(1)).trainingDuration(Duration.ofMinutes(45)).build();
        finalValue = training;
        Training finalValue4 = finalValue;
        assertThrows(IncorrectMethodArgumentException.class, () -> service.createTraining(finalValue4));
        verifyNoInteractions(trainingRepository);
    }

    @Test
    void missingTraineeTrainerOrTypeIsRejected() {
        assertThrows(EntityNotFoundException.class, () -> service.createTraining(training()));
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(new TraineeEntity()));
        assertThrows(EntityNotFoundException.class, () -> service.createTraining(training()));
        when(trainerRepository.findById(2L)).thenReturn(Optional.of(new TrainerEntity()));
        assertThrows(EntityNotFoundException.class, () -> service.createTraining(training()));
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void inactiveTraineeAndTrainerAreRejected() {
        Training training = training();
        Trainee inactive = activeTrainee();
        inactive.getUser().setActive(false);
        stubParticipants(training, inactive, activeTrainer(3L));
        assertThrows(BusinessRuleViolationException.class, () -> service.createTraining(training));
    }

    @Test
    void specializationMismatchIsRejected() {
        Training training = training();
        stubParticipants(training, activeTrainee(), activeTrainer(9L));
        assertThrows(BusinessRuleViolationException.class, () -> service.createTraining(training));
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void getByIdMapsEntityOrThrows() {
        TrainingEntity trainingEntity = new TrainingEntity();
        Training domain = training();
        when(trainingRepository.findById(10L)).thenReturn(Optional.of(trainingEntity));
        when(trainingMapper.toTrainingDomain(trainingEntity)).thenReturn(domain);
        assertSame(domain, service.getTrainingById(10L));
        assertThrows(EntityNotFoundException.class, () -> service.getTrainingById(99L));
    }
}
