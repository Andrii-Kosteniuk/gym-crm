package com.training.gym.repository;

import com.training.gym.domain.criteria.TraineeTrainingFilter;
import com.training.gym.domain.criteria.TrainerTrainingFilter;
import com.training.gym.persistence.entity.TrainingEntity;

import java.util.List;
import java.util.Optional;

public interface TrainingRepository  {

    TrainingEntity save(TrainingEntity training);

    Optional<TrainingEntity> findById(Long id);

    List<TrainingEntity> findAll();

    boolean existsById(Long id);

    List<TrainingEntity> findByTraineeUsername(String username, TraineeTrainingFilter filter);

    List<TrainingEntity> findByTrainerUsername(String username, TrainerTrainingFilter filter);
}


