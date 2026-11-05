package com.training.gym.facade;

import com.training.gym.model.Trainee;
import com.training.gym.dto.trainee.CreateTraineeDto;
import com.training.gym.dto.trainee.UpdateTraineeDto;
import com.training.gym.service.TraineeService;
import com.training.gym.model.Trainer;
import com.training.gym.dto.trainer.CreateTrainerDto;
import com.training.gym.dto.trainer.UpdateTrainerDto;
import com.training.gym.service.TrainerService;
import com.training.gym.model.Training;
import com.training.gym.dto.training.CreateTrainingDto;
import com.training.gym.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GymCrmFacade {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public Trainee createTrainee(CreateTraineeDto dto) {
        return traineeService.createTrainee(dto);
    }

    public Trainee updateTrainee(UpdateTraineeDto dto) {
        return traineeService.updateTrainee(dto);
    }

    public Trainee getTraineeById(Long id) {
        return traineeService.getTraineeById(id);
    }

    public void deleteTrainee(Long id) {
        traineeService.removeTrainee(id);
    }

    public Trainer createTrainer(CreateTrainerDto dto) {
        return trainerService.createTrainer(dto);
    }

    public Trainer updateTrainer(UpdateTrainerDto dto) {
        return trainerService.updateTrainer(dto);
    }

    public Trainer getTrainerById(Long id) {
        return trainerService.getTrainerById(id);
    }

    public Training createTraining(CreateTrainingDto dto) {
        return trainingService.createTraining(dto);
    }

    public Training getTrainingById(Long id) {
        return trainingService.getTrainingById(id);
    }


}


