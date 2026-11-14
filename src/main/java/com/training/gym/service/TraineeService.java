package com.training.gym.service;

import com.training.gym.dto.trainee.TraineeTrainingCriteria;
import com.training.gym.model.Trainee;
import com.training.gym.model.Trainer;
import com.training.gym.model.Training;

import java.util.List;
import java.util.Set;

public interface TraineeService {

    RegistrationResult<Trainee> registerTrainee(Trainee trainee);

    Trainee updateTrainee(Trainee trainee);

    Trainee getTraineeById(Long id);

    Trainee findByUserName(String username);

    void removeTraineeByUsername(String username);

    void activateTrainee(Trainee trainee);

    void deactivateTrainee(Trainee trainee);

    List<Training> findTrainings(String username, TraineeTrainingCriteria criteria);

    void changePassword(String username, String oldPassword, String newPassword);

    Set<Trainer> replaceTrainers(String username, Set<Long> ids);
}
