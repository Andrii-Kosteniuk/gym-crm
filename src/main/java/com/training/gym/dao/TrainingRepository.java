package com.training.gym.dao;

import com.training.gym.model.Training;

public interface TrainingRepository extends CrudRepository<Training, Long> {

    boolean existsByTraineeId(Long traineeId);

    boolean existsByTrainerId(Long trainerId);
}


