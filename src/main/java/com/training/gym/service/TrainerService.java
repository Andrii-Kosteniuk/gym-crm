package com.training.gym.service;

import com.training.gym.dto.trainer.TrainerTrainingCriteria;
import com.training.gym.model.Trainer;
import com.training.gym.model.Training;

import java.util.List;

public interface TrainerService {

    RegistrationResult<Trainer> registerTrainer(Trainer trainer);

    Trainer updateTrainer(Trainer trainer);

    Trainer getTrainerById(Long id);

    Trainer findByUserName(String username);

    void activateTrainer(Trainer trainer);

    void deactivateTrainer(Trainer trainer);

    List<Training> findTrainings(String username, TrainerTrainingCriteria criteria);

    List<Trainer> getUnassignedTrainers(String username);

    void changePassword(String username, String oldPassword, String newPassword);
}
