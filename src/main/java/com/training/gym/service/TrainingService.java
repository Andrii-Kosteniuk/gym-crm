package com.training.gym.service;

import com.training.gym.model.Training;
import com.training.gym.dto.training.CreateTrainingDto;

public interface TrainingService {
    Training createTraining(CreateTrainingDto trainingDto);
    Training getTrainingById(Long id);
}


