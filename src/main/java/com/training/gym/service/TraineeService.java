package com.training.gym.service;

import com.training.gym.model.Trainee;
import com.training.gym.dto.trainee.CreateTraineeDto;
import com.training.gym.dto.trainee.UpdateTraineeDto;

public interface TraineeService {

    Trainee createTrainee(CreateTraineeDto trainee);
    Trainee updateTrainee(UpdateTraineeDto trainee);
    Trainee getTraineeById(Long id);
    void removeTrainee(Long id);
}
