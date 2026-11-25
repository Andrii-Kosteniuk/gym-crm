package com.training.gym.service.trainer;

import com.training.gym.domain.model.Trainer;
import com.training.gym.domain.model.Training;
import com.training.gym.dto.auth.RegistrationResult;
import com.training.gym.dto.crireria.TrainerTrainingCriteria;

import java.util.List;

public interface TrainerService {

    RegistrationResult<Trainer> registerTrainer(Trainer trainer);

    Trainer updateTrainer(Trainer trainer);

    Trainer getTrainerById(Long id);

    Trainer findByUserName(String username);

    void changeProfileStatus(String username, boolean active);

    List<Training> findTrainings(String username, TrainerTrainingCriteria criteria);

    List<Trainer> getUnassignedTrainers(String username);

    void changePassword(String username, String oldPassword, String newPassword);
}
