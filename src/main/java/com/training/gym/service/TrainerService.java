package com.training.gym.service;

import com.training.gym.model.Trainer;
import com.training.gym.dto.trainer.CreateTrainerDto;
import com.training.gym.dto.trainer.UpdateTrainerDto;

public interface TrainerService {

    Trainer createTrainer(CreateTrainerDto trainerDto);
    Trainer updateTrainer(UpdateTrainerDto trainerDto);
    Trainer getTrainerById(Long id);
}
