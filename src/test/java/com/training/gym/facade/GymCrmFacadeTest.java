package com.training.gym.facade;

import com.training.gym.dto.trainee.CreateTraineeDto;
import com.training.gym.dto.trainee.TraineeResponseDto;
import com.training.gym.dto.trainee.UpdateTraineeDto;
import com.training.gym.dto.trainer.CreateTrainerDto;
import com.training.gym.dto.trainer.TrainerResponseDto;
import com.training.gym.dto.trainer.UpdateTrainerDto;
import com.training.gym.dto.training.CreateTrainingDto;
import com.training.gym.dto.training.TrainingResponseDto;
import com.training.gym.mapper.TraineeMapper;
import com.training.gym.mapper.TrainerMapper;
import com.training.gym.mapper.TrainingMapper;
import com.training.gym.model.Trainee;
import com.training.gym.model.Trainer;
import com.training.gym.model.Training;
import com.training.gym.service.TraineeService;
import com.training.gym.service.TrainerService;
import com.training.gym.service.TrainingService;
import com.training.gym.validation.TraineeDtoValidator;
import com.training.gym.validation.TrainerDtoValidator;
import com.training.gym.validation.TrainingDtoValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GymCrmFacadeTest {
    @Mock
    TraineeService traineeService;
    @Mock
    TrainerService trainerService;
    @Mock
    TrainingService trainingService;
    @Mock
    TraineeDtoValidator traineeValidator;
    @Mock
    TrainerDtoValidator trainerValidator;
    @Mock
    TrainingDtoValidator trainingValidator;
    @Mock
    TraineeMapper traineeMapper;
    @Mock
    TrainerMapper trainerMapper;
    @Mock
    TrainingMapper trainingMapper;
    @InjectMocks
    GymCrmFacade facade;

    @Test
    void shouldValidateMapAndCreateTraineeInOrder() {
        // Arrange
        CreateTraineeDto dto = mock(CreateTraineeDto.class);
        TraineeResponseDto response = mock(TraineeResponseDto.class);
        Trainee mapped = new Trainee();
        Trainee saved = new Trainee();
        when(traineeMapper.toTrainee(dto)).thenReturn(mapped);
        when(traineeService.createTrainee(mapped)).thenReturn(saved);
        when(traineeMapper.toResponseDto(mapped)).thenReturn(response);
        // Act
        var result = facade.createTrainee(dto);
        // Assert
        assertSame(response, result);
        InOrder order = inOrder(traineeValidator, traineeMapper, traineeService);
        order.verify(traineeValidator).validateCreate(dto);
        order.verify(traineeMapper).toTrainee(dto);
        order.verify(traineeService).createTrainee(mapped);
    }

    @Test
    void shouldValidateMapAndUpdateTraineeInOrder() {
        // Arrange
        UpdateTraineeDto dto = mock(UpdateTraineeDto.class);
        TraineeResponseDto response = mock(TraineeResponseDto.class);
        Trainee mapped = new Trainee();
        when(traineeMapper.toUpdateTrainee(dto)).thenReturn(mapped);
        when(traineeService.updateTrainee(mapped)).thenReturn(mapped);
        when(traineeMapper.toResponseDto(mapped)).thenReturn(response);
        // Act
        var result = facade.updateTrainee(dto);
        // Assert
        assertSame(response, result);
        InOrder order = inOrder(traineeValidator, traineeMapper, traineeService);
        order.verify(traineeValidator).validateUpdate(dto);
        order.verify(traineeMapper).toUpdateTrainee(dto);
        order.verify(traineeService).updateTrainee(mapped);
    }

    @Test
    void shouldValidateMapAndCreateTrainerInOrder() {
        // Arrange
        CreateTrainerDto dto = mock(CreateTrainerDto.class);
        TrainerResponseDto response = mock(TrainerResponseDto.class);
        Trainer mapped = new Trainer();
        when(trainerMapper.toTrainer(dto)).thenReturn(mapped);
        when(trainerService.createTrainer(mapped)).thenReturn(mapped);
        when(trainerMapper.toResponseDto(mapped)).thenReturn(response);
        // Act
        var result = facade.createTrainer(dto);
        // Assert
        assertSame(response, result);
        InOrder order = inOrder(trainerValidator, trainerMapper, trainerService);
        order.verify(trainerValidator).validateCreate(dto);
        order.verify(trainerMapper).toTrainer(dto);
        order.verify(trainerService).createTrainer(mapped);
    }

    @Test
    void shouldValidateMapAndUpdateTrainerInOrder() {
        // Arrange
        UpdateTrainerDto dto = mock(UpdateTrainerDto.class);
        TrainerResponseDto response = mock(TrainerResponseDto.class);
        Trainer mapped = new Trainer();
        when(trainerMapper.updateTrainer(dto)).thenReturn(mapped);
        when(trainerService.updateTrainer(mapped)).thenReturn(mapped);
        when(trainerMapper.toResponseDto(mapped)).thenReturn(response);
        // Act
        var result = facade.updateTrainer(dto);
        // Assert
        assertSame(response, result);
        InOrder order = inOrder(trainerValidator, trainerMapper, trainerService);
        order.verify(trainerValidator).validateUpdate(dto);
        order.verify(trainerMapper).updateTrainer(dto);
        order.verify(trainerService).updateTrainer(mapped);
    }

    @Test
    void shouldValidateMapAndCreateTrainingInOrder() {
        // Arrange
        CreateTrainingDto dto = mock(CreateTrainingDto.class);
        TrainingResponseDto response = mock(TrainingResponseDto.class);
        Training mapped = new Training();
        when(trainingMapper.toTraining(dto)).thenReturn(mapped);
        when(trainingService.createTraining(mapped)).thenReturn(mapped);
        when(trainingMapper.toResponseDto(mapped)).thenReturn(response);
        // Act
        var result = facade.createTraining(dto);
        // Assert
        assertSame(response, result);
        InOrder order = inOrder(trainingValidator, trainingMapper, trainingService);
        order.verify(trainingValidator).validateCreate(dto);
        order.verify(trainingMapper).toTraining(dto);
        order.verify(trainingService).createTraining(mapped);
    }

    @Test
    void shouldNotMapOrCallServiceWhenTraineeValidationFails() {
        // Arrange
        CreateTraineeDto dto = mock(CreateTraineeDto.class);
        doThrow(new IllegalArgumentException("Invalid trainee"))
                .when(traineeValidator).validateCreate(dto);
        // Act
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> facade.createTrainee(dto));
        // Assert
        assertEquals("Invalid trainee", error.getMessage());
        verifyNoInteractions(traineeMapper, traineeService);
    }

    @Test
    void shouldNotMapOrCallServiceWhenTrainerValidationFails() {
        // Arrange
        CreateTrainerDto dto = mock(CreateTrainerDto.class);
        doThrow(new IllegalArgumentException("Invalid trainer"))
                .when(trainerValidator).validateCreate(dto);
        // Act
        assertThrows(IllegalArgumentException.class, () -> facade.createTrainer(dto));
        // Assert
        verifyNoInteractions(trainerMapper, trainerService);
    }

    @Test
    void shouldNotMapOrCallServiceWhenTrainingValidationFails() {
        // Arrange
        CreateTrainingDto dto = mock(CreateTrainingDto.class);
        doThrow(new IllegalArgumentException("Invalid training"))
                .when(trainingValidator).validateCreate(dto);
        // Act
        assertThrows(IllegalArgumentException.class, () -> facade.createTraining(dto));
        // Assert
        verifyNoInteractions(trainingMapper, trainingService);
    }
}
