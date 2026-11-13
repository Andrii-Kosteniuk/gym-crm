package com.training.gym.service;

import com.training.gym.model.Trainee;

public interface TraineeService {

    Trainee createTrainee(Trainee trainee);
    Trainee updateTrainee(Trainee trainee);
    Trainee getTraineeById(Long id);
    void removeTrainee(Long id);
}
