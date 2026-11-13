package com.training.gym.service;

import com.training.gym.model.Trainer;

public interface TrainerService {

    Trainer createTrainer(Trainer trainer);
    Trainer updateTrainer(Trainer trainer);
    Trainer getTrainerById(Long id);
}
