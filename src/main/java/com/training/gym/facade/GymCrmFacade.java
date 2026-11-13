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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GymCrmFacade {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TraineeDtoValidator traineeValidator;
    private final TrainerDtoValidator trainerValidator;
    private final TrainingDtoValidator trainingValidator;
    private final TraineeMapper traineeMapper;
    private final TrainerMapper trainerMapper;
    private final TrainingMapper trainingMapper;


    public TraineeResponseDto createTrainee(CreateTraineeDto dto) {

        traineeValidator.validateCreate(dto);

        Trainee trainee = traineeMapper.toTrainee(dto);

        Trainee createdTrainee = traineeService.createTrainee(trainee);

        return traineeMapper.toResponseDto(createdTrainee);
    }

    public TraineeResponseDto updateTrainee(UpdateTraineeDto dto) {

        traineeValidator.validateUpdate(dto);

        Trainee trainee = traineeMapper.toUpdateTrainee(dto);

        Trainee updatedTrainee = traineeService.updateTrainee(trainee);

        return traineeMapper.toResponseDto(updatedTrainee);
    }

    public TraineeResponseDto getTraineeById(Long id) {
        Trainee traineeById = traineeService.getTraineeById(id);

        return traineeMapper.toResponseDto(traineeById);
    }

    public void deleteTrainee(Long id) {
        traineeService.removeTrainee(id);
    }

    public TrainerResponseDto createTrainer(CreateTrainerDto dto) {
        trainerValidator.validateCreate(dto);

        Trainer trainer = trainerMapper.toTrainer(dto);

        Trainer createdTrainer = trainerService.createTrainer(trainer);

        return trainerMapper.toResponseDto(createdTrainer);
    }

    public TrainerResponseDto updateTrainer(UpdateTrainerDto dto) {
        trainerValidator.validateUpdate(dto);

        Trainer trainer = trainerMapper.updateTrainer(dto);

        Trainer updatedTrainer = trainerService.updateTrainer(trainer);

        return trainerMapper.toResponseDto(updatedTrainer);
    }

    public TrainerResponseDto getTrainerById(Long id) {
        Trainer trainerById = trainerService.getTrainerById(id);

        return trainerMapper.toResponseDto(trainerById);
    }

    public TrainingResponseDto createTraining(CreateTrainingDto dto) {
        trainingValidator.validateCreate(dto);

        Training training = trainingMapper.toTraining(dto);

        Training createdTraining = trainingService.createTraining(training);
        return trainingMapper.toResponseDto(createdTraining);
    }

    public TrainingResponseDto getTrainingById(Long id) {
        Training trainingById = trainingService.getTrainingById(id);

        return trainingMapper.toResponseDto(trainingById);
    }


}


