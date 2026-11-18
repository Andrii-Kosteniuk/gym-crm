package com.training.gym.repository;

import com.training.gym.domain.Training;
import com.training.gym.domain.criteria.TraineeTrainingFilter;
import com.training.gym.domain.criteria.TrainerTrainingFilter;

import java.util.List;

public interface TrainingRepository extends CrudRepository<Training, Long> {

    List<Training> findByTraineeUsername(String username, TraineeTrainingFilter filter);

    List<Training> findByTrainerUsername(String username, TrainerTrainingFilter filter);
}


