package com.training.gym.service.training;

import com.training.gym.domain.model.Training;

public interface TrainingService {
    Training createTraining(Training training);
    Training getTrainingById(Long id);
}


