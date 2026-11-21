package com.training.gym.service.trainee;

import com.training.gym.domain.model.Trainee;
import com.training.gym.domain.model.Training;
import com.training.gym.dto.auth.RegistrationResult;
import com.training.gym.dto.crireria.TraineeTrainingCriteria;


import java.util.List;
import java.util.Set;

public interface TraineeService {

    RegistrationResult<Trainee> registerTrainee(Trainee trainee);

    Trainee updateTrainee(Trainee trainee);

    Trainee getTraineeById(Long id);

    Trainee findByUserName(String username);

    void removeTraineeByUsername(String username);

    void changeProfileStatus(String username, boolean active);

    List<Training> findTrainings(String username, TraineeTrainingCriteria criteria);

    void changePassword(String username, String oldPassword, String newPassword);

    void replaceTrainers(String username, Set<Long> trainersIds);
}
