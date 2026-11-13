package com.training.gym.service;

import com.training.gym.model.Training;

public interface TrainingService {
    Training createTraining(Training training);
    Training getTrainingById(Long id);
}


